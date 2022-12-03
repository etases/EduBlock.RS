package io.github.etases.edublock.rs.handler;

import io.github.etases.edublock.rs.RequestServer;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.Record;
import io.github.etases.edublock.rs.entity.*;
import io.github.etases.edublock.rs.internal.pagination.PaginationUtil;
import io.github.etases.edublock.rs.internal.subject.Subject;
import io.github.etases.edublock.rs.internal.subject.SubjectManager;
import io.github.etases.edublock.rs.model.input.PaginationParameter;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryInput;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryListInput;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryVerify;
import io.github.etases.edublock.rs.model.output.*;
import io.github.etases.edublock.rs.model.output.element.PendingRecordEntryOutput;
import io.github.etases.edublock.rs.model.output.element.RecordHistoryOutput;
import io.github.etases.edublock.rs.model.output.element.RecordOutput;
import io.github.etases.edublock.rs.model.output.element.RecordWithStudentOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RecordHandler extends SimpleServerHandler {

    private final SessionFactory sessionFactory;
    private final RequestServer requestServer;

    @Inject
    public RecordHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory, RequestServer requestServer) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
        this.requestServer = requestServer;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.post("/record/request", this::request, JwtHandler.Role.STUDENT, JwtHandler.Role.TEACHER);
        server.post("/record/request/list", this::bulkRequest, JwtHandler.Role.STUDENT, JwtHandler.Role.TEACHER);
        server.post("/record/entry", this::updateEntry, JwtHandler.Role.TEACHER);
        server.get("/record/pending/list", this::listPending, JwtHandler.Role.TEACHER);
        server.get("/record/pending/list/{studentId}", this::listPendingByStudent, JwtHandler.Role.TEACHER);
        server.post("/record/pending/verify", this::verify, JwtHandler.Role.TEACHER);
        server.get("/record/{classroomId}", this::getOwn, JwtHandler.Role.STUDENT);
        server.get("/record/{classroomId}/{studentId}", this::get, JwtHandler.Role.TEACHER, JwtHandler.Role.ADMIN, JwtHandler.Role.STAFF);
        server.get("/record/list/classroom/{classroomId}", this::listByClassroom, JwtHandler.Role.TEACHER, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
        server.get("/record/list/grade/{grade}/{year}", this::listByGradeAndYear, JwtHandler.Role.TEACHER, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
    }

    private CompletableFuture<RecordOutput> insertRecordFromUpdater(long studentId, RecordOutput recordOutput) {
        var studentUpdater = requestServer.getHandler(StudentUpdateHandler.class).getStudentUpdater();
        return studentUpdater.getStudentRecordHistory(studentId).thenApply(recordHistories -> {
            var recordEntryOutputsFromHistory = recordHistories.parallelStream()
                    .map(RecordHistoryOutput::fromFabricModel)
                    .flatMap(history -> history.getRecord().parallelStream())
                    .filter(record -> record.getClassroom().getId() == recordOutput.getClassroom().getId())
                    .flatMap(record -> record.getEntries().parallelStream())
                    .toList();
            var joinedRecordEntryOutputs = new ArrayList<>(recordOutput.getEntries());
            joinedRecordEntryOutputs.addAll(recordEntryOutputsFromHistory);
            recordOutput.setEntries(joinedRecordEntryOutputs);
            return recordOutput;
        });
    }

    private CompletableFuture<RecordWithStudentOutput> insertRecordFromUpdater(long studentId, RecordWithStudentOutput recordOutput) {
        var studentUpdater = requestServer.getHandler(StudentUpdateHandler.class).getStudentUpdater();
        return studentUpdater.getStudentRecordHistory(studentId).thenApply(recordHistories -> {
            var recordEntryOutputsFromHistory = recordHistories.parallelStream()
                    .map(RecordHistoryOutput::fromFabricModel)
                    .flatMap(history -> history.getRecord().parallelStream())
                    .filter(record -> record.getClassroom().getId() == recordOutput.getClassroom().getId())
                    .flatMap(record -> record.getEntries().parallelStream())
                    .toList();
            var joinedRecordEntryOutputs = new ArrayList<>(recordOutput.getEntries());
            joinedRecordEntryOutputs.addAll(recordEntryOutputsFromHistory);
            recordOutput.setEntries(joinedRecordEntryOutputs);
            return recordOutput;
        });
    }

    private Record createEmptyRecord(Student student, Classroom classroom) {
        var record = new Record();
        record.setStudent(student);
        record.setClassroom(classroom);
        record.setRecordEntry(new ArrayList<>());
        record.setPendingRecordEntry(new ArrayList<>());
        return record;
    }

    private Record createEmptyRecord(long studentId, long classroomId) {
        try (var session = sessionFactory.openSession()) {
            var student = session.get(Student.class, studentId);
            var classroom = session.get(Classroom.class, classroomId);
            if (student == null || classroom == null) {
                return null;
            }
            var transaction = session.beginTransaction();
            var record = createEmptyRecord(student, classroom);
            session.save(record);
            transaction.commit();
            return record;
        }
    }

    private Record getOrCreateRecord(Session session, Student student, Classroom classroom) {
        var recordQuery = session.createNamedQuery("Record.findByStudentAndClassroom", Record.class)
                .setParameter("studentId", student.getId())
                .setParameter("classroomId", classroom.getId());
        return recordQuery.uniqueResultOptional().orElseGet(() -> {
            var newRecord = createEmptyRecord(student, classroom);
            session.save(newRecord);
            return newRecord;
        });
    }

    private void get(Context ctx, boolean isOwnRecordOnly) {
        long studentId = isOwnRecordOnly ? JwtHandler.getUserId(ctx) : Long.parseLong(ctx.pathParam("studentId"));
        long classroomId = Long.parseLong(ctx.pathParam("classroomId"));
        boolean useUpdater = "true".equalsIgnoreCase(ctx.queryParam("updater"));
        boolean filterUpdated = "true".equalsIgnoreCase(ctx.queryParam("filterUpdated"));
        boolean generateClassification = "true".equalsIgnoreCase(ctx.queryParam("generateClassification"));
        boolean fillAllSubjects = "true".equalsIgnoreCase(ctx.queryParam("fillAllSubjects"));

        RecordOutput recordOutput;
        try (var session = sessionFactory.openSession()) {
            var query = session.createNamedQuery("Record.findByStudentAndClassroom", Record.class)
                    .setParameter("studentId", studentId)
                    .setParameter("classroomId", classroomId);
            var record = query.uniqueResultOptional().orElseGet(() -> createEmptyRecord(studentId, classroomId));
            if (record == null) {
                ctx.status(404);
                ctx.json(new RecordResponse(1, "Record not found", null));
                return;
            }
            recordOutput = RecordOutput.fromEntity(record, id -> Profile.getOrDefault(session, id), filterUpdated, fillAllSubjects);
        }

        if (useUpdater) {
            ctx.future(() -> insertRecordFromUpdater(studentId, recordOutput).thenAccept(newRecordOutput -> {
                if (generateClassification) {
                    newRecordOutput.updateClassification();
                }
                ctx.json(new RecordResponse(0, "Get personal record", newRecordOutput));
            }));
        } else {
            if (generateClassification) {
                recordOutput.updateClassification();
            }
            ctx.json(new RecordResponse(0, "Get personal record", recordOutput));
        }
    }

    @OpenApi(
            path = "/record/{classroomId}/{studentId}",
            methods = HttpMethod.GET,
            summary = "Get student record. Roles: TEACHER, ADMIN, STAFF",
            description = "Get student record. Roles: TEACHER, ADMIN, STAFF",
            tags = "Record",
            pathParams = {
                    @OpenApiParam(name = "classroomId", description = "Classroom ID", required = true),
                    @OpenApiParam(name = "studentId", description = "Student ID", required = true)
            },
            queryParams = {
                    @OpenApiParam(name = "updater", description = "Add entries from updater"),
                    @OpenApiParam(name = "filterUpdated", description = "Filter local updated entries"),
                    @OpenApiParam(name = "generateClassification", description = "Generate classification"),
                    @OpenApiParam(name = "fillAllSubjects", description = "Fill all subjects")
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = RecordResponse.class),
                            description = "The record"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = RecordResponse.class),
                            description = "Record not found"
                    )
            }
    )
    private void get(Context ctx) {
        get(ctx, false);
    }

    @OpenApi(
            path = "/record/{classroomId}",
            methods = HttpMethod.GET,
            summary = "Get own record. Roles: STUDENT",
            description = "Get own record. Roles: STUDENT",
            tags = "Record",
            pathParams = @OpenApiParam(name = "classroomId", description = "Classroom ID", required = true),
            queryParams = {
                    @OpenApiParam(name = "updater", description = "Add entries from updater"),
                    @OpenApiParam(name = "filterUpdated", description = "Filter local updated entries"),
                    @OpenApiParam(name = "generateClassification", description = "Generate classification"),
                    @OpenApiParam(name = "fillAllSubjects", description = "Fill all subjects")
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = RecordResponse.class),
                            description = "The record"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = RecordResponse.class),
                            description = "Record not found"
                    )
            }
    )
    private void getOwn(Context ctx) {
        get(ctx, true);
    }

    private Optional<PendingRecordEntryErrorListResponse.ErrorData> tryRequest(Session session, Account requester, PendingRecordEntryInput input) {
        Subject subject = SubjectManager.getSubject(input.getSubjectId());
        if (subject == null) {
            return Optional.of(new PendingRecordEntryErrorListResponse.ErrorData(1, "Subject not found", input));
        }

        var classStudentQuery = session.createNamedQuery("ClassStudent.findByClassroomAndStudent", ClassStudent.class)
                .setParameter("classroomId", input.getClassroomId())
                .setParameter("studentId", input.getStudentId());
        var classStudent = classStudentQuery.uniqueResult();
        if (classStudent == null) {
            return Optional.of(new PendingRecordEntryErrorListResponse.ErrorData(2, "ClassStudent not found", input));
        }
        var student = classStudent.getStudent();
        var classroom = classStudent.getClassroom();

        var classTeacherQuery = session.createNamedQuery("ClassTeacher.findByClassroomAndSubject", ClassTeacher.class)
                .setParameter("classroomId", input.getClassroomId())
                .setParameter("subjectId", input.getSubjectId());
        var classTeacher = classTeacherQuery.uniqueResult();
        if (classTeacher == null) {
            return Optional.of(new PendingRecordEntryErrorListResponse.ErrorData(3, "ClassTeacher not found", input));
        }
        var teacher = classTeacher.getTeacher();

        var record = getOrCreateRecord(session, student, classroom);

        var pending = new PendingRecordEntry();
        pending.setSubjectId(subject.getId());
        pending.setFirstHalfScore(input.getFirstHalfScore());
        pending.setSecondHalfScore(input.getSecondHalfScore());
        pending.setFinalScore(input.getFinalScore());
        pending.setTeacher(teacher);
        pending.setRequester(requester);
        pending.setRequestDate(new Date());
        pending.setRecord(record);
        session.save(pending);

        return Optional.empty();
    }

    @OpenApi(
            path = "/record/request",
            methods = HttpMethod.POST,
            summary = "Request record update. Roles: STUDENT, TEACHER",
            description = "Request record update. Roles: STUDENT, TEACHER",
            tags = "Record",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(
                    content = @OpenApiContent(from = PendingRecordEntryInput.class)
            ),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = Response.class),
                    description = "Student request record validation"
            )
    )
    private void request(Context ctx) {
        PendingRecordEntryInput input = ctx.bodyValidator(PendingRecordEntryInput.class).check(PendingRecordEntryInput::validate, "Invalid data").get();
        long userId = JwtHandler.getUserId(ctx);

        try (var session = sessionFactory.openSession()) {
            var requester = session.get(Account.class, userId);

            Transaction transaction = session.beginTransaction();

            var optionalErrorData = tryRequest(session, requester, input);
            if (optionalErrorData.isPresent()) {
                var errorData = optionalErrorData.get();
                ctx.status(404);
                ctx.json(new Response(errorData.getStatus(), errorData.getMessage()));
                transaction.rollback();
                return;
            }

            transaction.commit();
            ctx.json(new Response(0, "Record validation requested"));
        }
    }

    @OpenApi(
            path = "/record/entry",
            methods = HttpMethod.POST,
            summary = "Update student record entry (Teacher with the subject). Roles: TEACHER",
            description = "Update student record entry (Teacher with the subject). Roles: TEACHER",
            tags = "Record",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(
                    content = @OpenApiContent(from = PendingRecordEntryInput.class)
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Success",
                            content = @OpenApiContent(from = Response.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Student not found",
                            content = @OpenApiContent(from = Response.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Teacher not found or not assigned to this subject",
                            content = @OpenApiContent(from = Response.class)
                    )
            }
    )
    private void updateEntry(Context ctx) {
        PendingRecordEntryInput input = ctx.bodyValidator(PendingRecordEntryInput.class).check(PendingRecordEntryInput::validate, "Invalid data").get();
        long userId = JwtHandler.getUserId(ctx);

        try (var session = sessionFactory.openSession()) {
            var classStudentQuery = session.createNamedQuery("ClassStudent.findByClassroomAndStudent", ClassStudent.class)
                    .setParameter("classroomId", input.getClassroomId())
                    .setParameter("studentId", input.getStudentId());
            var classStudent = classStudentQuery.uniqueResult();
            if (classStudent == null) {
                ctx.status(404);
                ctx.json(new Response(1, "Student not found"));
                return;
            }
            var student = classStudent.getStudent();
            var classroom = classStudent.getClassroom();

            var classTeacherQuery = session.createNamedQuery("ClassTeacher.findByClassroomAndTeacherAndSubject", ClassTeacher.class)
                    .setParameter("classroomId", input.getClassroomId())
                    .setParameter("teacherId", userId)
                    .setParameter("subjectId", input.getSubjectId());
            var classTeacher = classTeacherQuery.uniqueResult();
            if (classTeacher == null) {
                ctx.status(404);
                ctx.json(new Response(2, "Teacher not found or not assigned to this subject"));
                return;
            }
            var teacher = classTeacher.getTeacher();

            Transaction transaction = session.beginTransaction();

            var record = getOrCreateRecord(session, student, classroom);

            var recordEntry = new RecordEntry();
            recordEntry.setSubjectId(input.getSubjectId());
            recordEntry.setFirstHalfScore(input.getFirstHalfScore());
            recordEntry.setSecondHalfScore(input.getSecondHalfScore());
            recordEntry.setFinalScore(input.getFinalScore());
            recordEntry.setTeacher(teacher);
            recordEntry.setRequester(teacher);
            recordEntry.setRecord(record);
            recordEntry.setRequestDate(new Date());
            recordEntry.setApprovalDate(new Date());
            recordEntry.setApprover(teacher);
            recordEntry.setUpdateComplete(false);
            session.save(recordEntry);

            transaction.commit();
            ctx.json(new Response(0, "Record entry updated"));
        }
    }

    @OpenApi(
            path = "/record/request/list",
            methods = HttpMethod.POST,
            summary = "Bulk request record update. Roles: STUDENT, TEACHER",
            description = "Bulk Request record update. Roles: STUDENT, TEACHER",
            tags = "Record",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(
                    content = @OpenApiContent(from = PendingRecordEntryListInput.class)
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The success response",
                            content = @OpenApiContent(from = PendingRecordEntryErrorListResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "The errors of the requests.",
                            content = @OpenApiContent(from = PendingRecordEntryErrorListResponse.class)
                    ),
            }
    )
    private void bulkRequest(Context ctx) {
        PendingRecordEntryListInput input = ctx.bodyValidator(PendingRecordEntryListInput.class).check(PendingRecordEntryListInput::validate, "Invalid data").get();
        long userId = JwtHandler.getUserId(ctx);

        try (var session = sessionFactory.openSession()) {
            var requester = session.get(Account.class, userId);

            Transaction transaction = session.beginTransaction();
            List<PendingRecordEntryErrorListResponse.ErrorData> errors = new ArrayList<>();
            for (PendingRecordEntryInput pendingRecordEntryInput : input.getRequests()) {
                tryRequest(session, requester, pendingRecordEntryInput).ifPresent(errors::add);
            }

            if (errors.isEmpty()) {
                transaction.commit();
                ctx.json(new PendingRecordEntryErrorListResponse(0, "Record validation requested", errors));
            } else {
                transaction.rollback();
                ctx.status(400);
                ctx.json(new PendingRecordEntryErrorListResponse(1, "There are errors in the requests", errors));
            }
        }
    }

    private void list(Context ctx, boolean filterByClassroom) {
        boolean useUpdater = "true".equalsIgnoreCase(ctx.queryParam("updater"));
        boolean filterUpdated = "true".equalsIgnoreCase(ctx.queryParam("filterUpdated"));
        boolean generateClassification = "true".equalsIgnoreCase(ctx.queryParam("generateClassification"));
        boolean fillAllSubjects = "true".equalsIgnoreCase(ctx.queryParam("fillAllSubjects"));

        Map<Long, RecordWithStudentOutput> recordOutputs = new HashMap<>();
        try (var session = sessionFactory.openSession()) {
            Query<Record> query;
            if (filterByClassroom) {
                long classroomId = Long.parseLong(ctx.pathParam("classroomId"));
                query = session.createNamedQuery("Record.findByClassroom", Record.class)
                        .setParameter("classroomId", classroomId);
            } else {
                int grade = Integer.parseInt(ctx.pathParam("grade"));
                int year = Integer.parseInt(ctx.pathParam("year"));
                query = session.createNamedQuery("Record.findByGradeAndYear", Record.class)
                        .setParameter("grade", grade)
                        .setParameter("year", year);
            }
            for (var record : query.list()) {
                recordOutputs.put(record.getStudent().getId(), RecordWithStudentOutput.fromEntity(record, id -> Profile.getOrDefault(session, id), filterUpdated, fillAllSubjects));
            }
        }

        if (useUpdater) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            recordOutputs.forEach((studentId, recordOutput) -> futures.add(insertRecordFromUpdater(studentId, recordOutput).thenAccept(newRecordOutput -> {
                if (generateClassification) {
                    newRecordOutput.updateClassification();
                }
            })));
            ctx.future(() -> CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> new ArrayList<>(recordOutputs.values()))
                    .thenAccept(newRecordOutputs -> ctx.json(new RecordWithStudentListResponse(0, "OK", newRecordOutputs)))
            );
        } else {
            if (generateClassification) {
                recordOutputs.values().forEach(RecordWithStudentOutput::updateClassification);
            }
            ctx.json(new RecordWithStudentListResponse(0, "OK", new ArrayList<>(recordOutputs.values())));
        }
    }


    @OpenApi(
            path = "/record/list/classroom/{classroomId}",
            methods = HttpMethod.GET,
            summary = "Get list of records by classroom. Roles: TEACHER, ADMIN, STAFF",
            description = "Get list of records by classroom. Roles: TEACHER, ADMIN, STAFF",
            tags = "Record",
            pathParams = {
                    @OpenApiParam(name = "classroomId", description = "Classroom ID", required = true)
            },
            queryParams = {
                    @OpenApiParam(name = "updater", description = "Add entries from updater"),
                    @OpenApiParam(name = "filterUpdated", description = "Filter local updated entries"),
                    @OpenApiParam(name = "generateClassification", description = "Generate classification"),
                    @OpenApiParam(name = "fillAllSubjects", description = "Fill all subjects")
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = RecordWithStudentListResponse.class),
                    description = "The list of records"
            )
    )
    private void listByClassroom(Context ctx) {
        list(ctx, true);
    }

    @OpenApi(
            path = "/record/list/grade/{grade}/{year}",
            methods = HttpMethod.GET,
            summary = "Get list of records by grade and year. Roles: TEACHER, ADMIN, STAFF",
            description = "Get list of records by grade and year. Roles: TEACHER, ADMIN, STAFF",
            tags = "Record",
            pathParams = {
                    @OpenApiParam(name = "grade", description = "Grade", required = true),
                    @OpenApiParam(name = "year", description = "Year", required = true)
            },
            queryParams = {
                    @OpenApiParam(name = "updater", description = "Add entries from updater"),
                    @OpenApiParam(name = "filterUpdated", description = "Filter local updated entries"),
                    @OpenApiParam(name = "generateClassification", description = "Generate classification"),
                    @OpenApiParam(name = "fillAllSubjects", description = "Fill all subjects")
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = RecordWithStudentListResponse.class),
                    description = "The list of records"
            )
    )
    private void listByGradeAndYear(Context ctx) {
        list(ctx, false);
    }

    private void listPending(Context ctx, boolean filterByStudent) {
        long userId = JwtHandler.getUserId(ctx);
        var paginationParameter = PaginationParameter.fromQuery(ctx);

        try (var session = sessionFactory.openSession()) {
            Query<PendingRecordEntry> query;
            if (filterByStudent) {
                long studentId = Long.parseLong(ctx.pathParam("studentId"));
                query = session.createNamedQuery("PendingRecordEntry.findByHomeroomTeacherAndStudent", PendingRecordEntry.class)
                        .setParameter("studentId", studentId)
                        .setParameter("teacherId", userId);
            } else {
                query = session.createNamedQuery("PendingRecordEntry.findByHomeroomTeacher", PendingRecordEntry.class)
                        .setParameter("teacherId", userId);
            }
            var records = query.getResultList();
            var pagedPair = PaginationUtil.getPagedList(records, paginationParameter);
            List<PendingRecordEntryOutput> list = new ArrayList<>();
            for (var record : pagedPair.getKey()) {
                list.add(PendingRecordEntryOutput.fromEntity(record, id -> Profile.getOrDefault(session, id)));
            }
            ctx.json(new PendingRecordEntryListResponse(0, "Get pending record entry list", pagedPair.getValue(), list));
        }
    }

    @OpenApi(
            path = "/record/pending/list",
            methods = HttpMethod.GET,
            summary = "Get list of pending record entries. Roles: TEACHER",
            description = "Get list of pending record entries. Roles: TEACHER",
            tags = "Record",
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size")
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = PendingRecordEntryListResponse.class),
                    description = "The list of records"
            )
    )
    private void listPending(Context ctx) {
        listPending(ctx, false);
    }

    @OpenApi(
            path = "/record/pending/list/{studentId}",
            methods = HttpMethod.GET,
            summary = "Get list of pending record entries of a student. Roles: TEACHER",
            description = "Get list of pending record entries of a student. Roles: TEACHER",
            tags = "Record",
            pathParams = @OpenApiParam(name = "studentId", description = "Student ID", required = true),
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size")
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = PendingRecordEntryListResponse.class),
                    description = "The list of pending record entries"
            )
    )
    private void listPendingByStudent(Context ctx) {
        listPending(ctx, true);
    }

    @OpenApi(
            path = "/record/pending/verify",
            methods = HttpMethod.POST,
            summary = "Verify a record entry. Roles: TEACHER",
            description = "Verify a record entry. Roles: TEACHER",
            tags = "Record",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = PendingRecordEntryVerify.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = Response.class),
                            description = "Record verified"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = Response.class),
                            description = "Record not found"
                    ),
                    @OpenApiResponse(
                            status = "403",
                            content = @OpenApiContent(from = Response.class),
                            description = "Not authorized"
                    )
            }
    )
    private void verify(Context ctx) {
        PendingRecordEntryVerify input = ctx.bodyValidator(PendingRecordEntryVerify.class)
                .check(PendingRecordEntryVerify::validate, "Invalid Record Entry id")
                .get();

        long userId = JwtHandler.getUserId(ctx);

        try (var session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            var account = session.get(Account.class, userId);
            var pendingRecordEntry = session.get(PendingRecordEntry.class, input.getId());
            if (pendingRecordEntry == null) {
                ctx.status(404);
                ctx.json(new Response(1, "Record not found"));
                return;
            }
            var homeRoomTeacher = pendingRecordEntry.getRecord().getClassroom().getHomeroomTeacher();
            if (homeRoomTeacher == null || homeRoomTeacher.getId() != userId) {
                ctx.status(403);
                ctx.json(new Response(2, "Not homeroom teacher"));
                return;
            }

            if (input.isAccepted()) {
                var recordEntry = new RecordEntry();
                recordEntry.setSubjectId(pendingRecordEntry.getSubjectId());
                recordEntry.setFirstHalfScore(pendingRecordEntry.getFirstHalfScore());
                recordEntry.setSecondHalfScore(pendingRecordEntry.getSecondHalfScore());
                recordEntry.setFinalScore(pendingRecordEntry.getFinalScore());
                recordEntry.setTeacher(pendingRecordEntry.getTeacher());
                recordEntry.setRequester(pendingRecordEntry.getRequester());
                recordEntry.setRecord(pendingRecordEntry.getRecord());
                recordEntry.setRequestDate(pendingRecordEntry.getRequestDate());
                recordEntry.setApprovalDate(new Date());
                recordEntry.setApprover(account);
                recordEntry.setUpdateComplete(false);
                session.save(recordEntry);
            }
            session.delete(pendingRecordEntry);
            transaction.commit();
            ctx.json(new Response(0, "Record verified"));
        }
    }
}
