package io.github.etases.edublock.rs.handler;

import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.Record;
import io.github.etases.edublock.rs.entity.*;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryInput;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryVerify;
import io.github.etases.edublock.rs.model.output.PendingRecordEntryListResponse;
import io.github.etases.edublock.rs.model.output.RecordResponse;
import io.github.etases.edublock.rs.model.output.Response;
import io.github.etases.edublock.rs.model.output.element.PendingRecordEntryOutput;
import io.github.etases.edublock.rs.model.output.element.RecordOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordHandler extends SimpleServerHandler {

    private final SessionFactory sessionFactory;

    @Inject
    public RecordHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.post("/record/request", this::request, JwtHandler.Role.STUDENT, JwtHandler.Role.TEACHER);
        server.get("/record/pending/list", this::listPending, JwtHandler.Role.TEACHER);
        server.get("/record/pending/list/{studentId}", this::listPendingByStudent, JwtHandler.Role.TEACHER);
        server.post("/record/pending/verify", this::verify, JwtHandler.Role.TEACHER);
        server.get("/record/{classroomId}", this::getOwn, JwtHandler.Role.STUDENT);
        server.get("/record/{classroomId}/{studentId}", this::get, JwtHandler.Role.TEACHER);
    }

    private void get(Context ctx, boolean isOwnRecordOnly) {
        try (var session = sessionFactory.openSession()) {
            long studentId = isOwnRecordOnly ? JwtHandler.getUserId(ctx) : Long.parseLong(ctx.pathParam("studentId"));

            long classroomId = Long.parseLong(ctx.pathParam("classroomId"));
            var query = session.createNamedQuery("Record.findByStudentAndClassroom", Record.class)
                    .setParameter("studentId", studentId)
                    .setParameter("classroomId", classroomId);
            var record = query.uniqueResult();
            if (record == null) {
                ctx.status(404);
                ctx.json(new RecordResponse(1, "Record not found", null));
                return;
            }
            var recordOutput = RecordOutput.fromEntity(record, id -> Profile.getOrDefault(session, id));
            ctx.json(new RecordResponse(0, "Get personal record", recordOutput));
        }
    }

    @OpenApi(
            path = "/record/{classroomId}",
            methods = HttpMethod.GET,
            summary = "Get own record. Roles: STUDENT",
            description = "Get own record. Roles: STUDENT",
            tags = "Record",
            pathParams = @OpenApiParam(name = "classroomId", description = "Classroom ID"),
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
            path = "/record/{classroomId}/{studentId}",
            methods = HttpMethod.GET,
            summary = "Get student record. Roles: TEACHER",
            description = "Get student record. Roles: TEACHER",
            tags = "Record",
            pathParams = {
                    @OpenApiParam(name = "classroomId", description = "Classroom ID"),
                    @OpenApiParam(name = "studentId", description = "Student ID")
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

            Subject subject = session.get(Subject.class, input.getSubjectId());
            if (subject == null) {
                ctx.status(404);
                ctx.json(new Response(1, "Subject not found"));
                return;
            }

            Student student = session.get(Student.class, input.getStudentId());
            if (student == null) {
                ctx.status(404);
                ctx.json(new Response(2, "Student not found"));
                return;
            }

            Classroom classroom = session.get(Classroom.class, input.getClassroomId());
            if (classroom == null) {
                ctx.status(404);
                ctx.json(new Response(3, "Classroom not found"));
                return;
            }

            var classTeacherQuery = session.createNamedQuery("ClassTeacher.findByClassroomAndSubject", ClassTeacher.class)
                    .setParameter("classroomId", input.getClassroomId())
                    .setParameter("subjectId", input.getSubjectId());
            var classTeacher = classTeacherQuery.uniqueResult();
            if (classTeacher == null) {
                ctx.status(404);
                ctx.json(new Response(4, "ClassTeacher not found"));
                return;
            }
            var teacher = classTeacher.getTeacher();

            var recordQuery = session.createNamedQuery("Record.findByStudentAndClassroom", Record.class)
                    .setParameter("studentId", input.getStudentId())
                    .setParameter("classroomId", input.getClassroomId());
            var record = recordQuery.uniqueResult();
            if (record == null) {
                record = new Record();
                record.setStudent(student);
                record.setClassroom(classroom);
                session.save(record);
            }

            Transaction transaction = session.beginTransaction();
            var pending = new PendingRecordEntry();

            pending.setSubject(subject);
            pending.setFirstHalfScore(input.getFirstHalfScore());
            pending.setSecondHalfScore(input.getSecondHalfScore());
            pending.setFinalScore(input.getFinalScore());
            pending.setTeacher(teacher);
            pending.setRequester(requester);
            pending.setRequestDate(new Date());
            pending.setRecord(record);

            session.save(input);
            transaction.commit();

            ctx.json(new Response(0, "Record validation requested"));
        }
    }

    private void listPending(Context ctx, boolean filterByStudent) {
        long userId = JwtHandler.getUserId(ctx);

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
            List<PendingRecordEntryOutput> list = new ArrayList<>();
            for (var record : records) {
                list.add(PendingRecordEntryOutput.fromEntity(record, id -> Profile.getOrDefault(session, id)));
            }
            ctx.json(new PendingRecordEntryListResponse(0, "Get pending record entry list", list));
        }
    }

    @OpenApi(
            path = "/record/pending/list",
            methods = HttpMethod.GET,
            summary = "Get list of pending record entries. Roles: TEACHER",
            description = "Get list of pending record entries. Roles: TEACHER",
            tags = "Record",
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
            pathParams = @OpenApiParam(name = "studentId", description = "Student ID"),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = PendingRecordEntryListResponse.class),
                    description = "The list of records"
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
            if (pendingRecordEntry.getRecord().getClassroom().getHomeroomTeacher().getId() != userId) {
                ctx.status(403);
                ctx.json(new Response(2, "Not homeroom teacher"));
                return;
            }

            if (input.isAccepted()) {
                var recordEntry = new RecordEntry();
                recordEntry.setSubject(pendingRecordEntry.getSubject());
                recordEntry.setFirstHalfScore(pendingRecordEntry.getFirstHalfScore());
                recordEntry.setSecondHalfScore(pendingRecordEntry.getSecondHalfScore());
                recordEntry.setFinalScore(pendingRecordEntry.getFinalScore());
                recordEntry.setTeacher(pendingRecordEntry.getTeacher());
                recordEntry.setRequester(pendingRecordEntry.getRequester());
                recordEntry.setRecord(pendingRecordEntry.getRecord());
                recordEntry.setRequestDate(pendingRecordEntry.getRequestDate());
                recordEntry.setApprovalDate(new Date());
                recordEntry.setApprover(account);
                session.save(recordEntry);
            }
            session.delete(pendingRecordEntry);
            transaction.commit();
            ctx.json(new Response(0, "Record verified"));
        }
    }
}
