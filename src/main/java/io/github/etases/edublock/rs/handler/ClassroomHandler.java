package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.entity.*;
import io.github.etases.edublock.rs.internal.filter.ListSessionInputFilter;
import io.github.etases.edublock.rs.internal.pagination.PaginationUtil;
import io.github.etases.edublock.rs.internal.subject.SubjectManager;
import io.github.etases.edublock.rs.model.input.*;
import io.github.etases.edublock.rs.model.output.*;
import io.github.etases.edublock.rs.model.output.element.AccountWithStudentProfileOutput;
import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import io.github.etases.edublock.rs.model.output.element.TeacherWithSubjectOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.*;

public class ClassroomHandler extends SimpleServerHandler {
    private static final ListSessionInputFilter<Classroom> CLASSROOMS_FILTER = ListSessionInputFilter.<Classroom>create()
            .addFilter("id", (input, o) -> Long.toString(o.getId()).equals(input))
            .addFilter("name", (input, o) -> o.getName().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("grade", (input, o) -> Integer.toString(o.getGrade()).equals(input))
            .addFilter("homeroomTeacherId", (input, o) -> o.getHomeroomTeacher() != null && Long.toString(o.getHomeroomTeacher().getId()).equals(input))
            .addFilter("homeroomTeacherUserName", (input, o) -> o.getHomeroomTeacher() != null && o.getHomeroomTeacher().getUsername().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("homeroomTeacherFirstName", (session, input, o) -> o.getHomeroomTeacher() != null && Profile.getOrDefault(session, o.getHomeroomTeacher().getId()).getFirstName().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("homeroomTeacherLastName", (session, input, o) -> o.getHomeroomTeacher() != null && Profile.getOrDefault(session, o.getHomeroomTeacher().getId()).getLastName().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("homeroomTeacherEmail", (session, input, o) -> o.getHomeroomTeacher() != null && Profile.getOrDefault(session, o.getHomeroomTeacher().getId()).getEmail().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("homeroomTeacherPhone", (session, input, o) -> o.getHomeroomTeacher() != null && Profile.getOrDefault(session, o.getHomeroomTeacher().getId()).getPhone().contains(input))
            .addFilter("year", (input, o) -> {
                if (input == null || input.isEmpty()) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    input = Integer.toString(calendar.get(Calendar.YEAR));
                }
                return Integer.toString(o.getYear()).equals(input);
            });
    private final SessionFactory sessionFactory;
    private final MainConfig mainConfig;

    @Inject
    public ClassroomHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory, MainConfig mainConfig) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
        this.mainConfig = mainConfig;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.post("/classroom", this::create, JwtHandler.Role.STAFF);
        server.get("/classroom", this::list, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
        server.get("/classroom/teacher", this::listTeacher, JwtHandler.Role.TEACHER);
        server.get("/classroom/student", this::listStudent, JwtHandler.Role.STUDENT);
        server.get("/classroom/student/{id}", this::listSpecificStudent, JwtHandler.Role.TEACHER, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
        server.get("/classroom/homeroom", this::listHomeroom, JwtHandler.Role.TEACHER);
        server.get("/classroom/{id}", this::get, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN, JwtHandler.Role.TEACHER, JwtHandler.Role.STUDENT);
        server.put("/classroom/{id}", this::update, JwtHandler.Role.STAFF);
        server.get("/classroom/{id}/teacher", this::teacherList, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN, JwtHandler.Role.TEACHER, JwtHandler.Role.STUDENT);
        server.post("/classroom/{id}/teacher", this::addTeacher, JwtHandler.Role.STAFF);
        server.delete("/classroom/{id}/teacher", this::removeTeacher, JwtHandler.Role.STAFF);
        server.get("/classroom/{id}/student", this::studentList, JwtHandler.Role.TEACHER, JwtHandler.Role.ADMIN, JwtHandler.Role.STAFF);
        server.post("/classroom/{id}/student", this::addStudent, JwtHandler.Role.STAFF);
        server.delete("/classroom/{id}/student", this::removeStudent, JwtHandler.Role.STAFF);
    }

    private void list(Context ctx, boolean isTeacher, boolean isStudent, boolean isHomeroom, boolean isSpecificStudent) {
        var paginationParameter = PaginationParameter.fromQuery(ctx);
        try (var session = sessionFactory.openSession()) {
            List<Classroom> classrooms;
            if (isTeacher) {
                long userId = JwtHandler.getUserId(ctx);
                var query = session.createNamedQuery("ClassTeacher.findByTeacher", ClassTeacher.class)
                        .setParameter("teacherId", userId);
                var classTeachers = query.getResultList();
                classrooms = classTeachers.stream().map(ClassTeacher::getClassroom).toList();
            } else if (isStudent) {
                long userId;
                if (isSpecificStudent) {
                    userId = Long.parseLong(ctx.pathParam("id"));
                } else {
                    userId = JwtHandler.getUserId(ctx);
                }
                var query = session.createNamedQuery("ClassStudent.findByStudent", ClassStudent.class)
                        .setParameter("studentId", userId);
                var classStudents = query.getResultList();
                classrooms = classStudents.stream().map(ClassStudent::getClassroom).toList();
            } else if (isHomeroom) {
                long userId = JwtHandler.getUserId(ctx);
                var query = session.createNamedQuery("Classroom.findByHomeroomTeacher", Classroom.class)
                        .setParameter("teacherId", userId);
                classrooms = query.getResultList();
            } else {
                var query = session.createNamedQuery("Classroom.findAll", Classroom.class);
                classrooms = query.getResultList();
            }
            var filtered = CLASSROOMS_FILTER.filter(session, classrooms, ctx);
            var pagedPair = PaginationUtil.getPagedList(filtered, paginationParameter);
            List<ClassroomOutput> list = new ArrayList<>();
            for (var classroom : pagedPair.getKey()) {
                list.add(ClassroomOutput.fromEntity(classroom, id -> Profile.getOrDefault(session, id)));
            }
            ctx.json(new ClassroomListResponse(0, "Get classroom list", pagedPair.getValue(), list));
        }
    }

    @OpenApi(
            path = "/classroom",
            methods = HttpMethod.GET,
            summary = "Get classroom list. Roles: STAFF, ADMIN",
            description = "Get classroom list. Roles: STAFF, ADMIN",
            tags = "Classroom",
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size"),
                    @OpenApiParam(name = "filter", description = "Filter Type"),
                    @OpenApiParam(name = "input", description = "Filter Input"),
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = ClassroomListResponse.class),
                    description = "The list of classroom"
            )
    )
    private void list(Context ctx) {
        list(ctx, false, false, false, false);
    }

    @OpenApi(
            path = "/classroom/teacher",
            methods = HttpMethod.GET,
            summary = "Get classroom list of teacher. Roles: TEACHER",
            description = "Get classroom list of teacher. Roles: TEACHER",
            tags = "Classroom",
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size"),
                    @OpenApiParam(name = "filter", description = "Filter Type"),
                    @OpenApiParam(name = "input", description = "Filter Input"),
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = ClassroomListResponse.class),
                    description = "The list of classroom"
            )
    )
    private void listTeacher(Context ctx) {
        list(ctx, true, false, false, false);
    }

    @OpenApi(
            path = "/classroom/student",
            methods = HttpMethod.GET,
            summary = "Get classroom list of student. Roles: STUDENT",
            description = "Get classroom list of student. Roles: STUDENT",
            tags = "Classroom",
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size"),
                    @OpenApiParam(name = "filter", description = "Filter Type"),
                    @OpenApiParam(name = "input", description = "Filter Input"),
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = ClassroomListResponse.class),
                    description = "The list of classroom"
            )
    )
    private void listStudent(Context ctx) {
        list(ctx, false, true, false, false);
    }

    @OpenApi(
            path = "/classroom/student/{id}",
            methods = HttpMethod.GET,
            summary = "Get classroom list of a student. Roles: TEACHER, ADMIN, STAFF",
            description = "Get classroom list of a student. Roles: TEACHER, ADMIN, STAFF",
            tags = "Classroom",
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size"),
                    @OpenApiParam(name = "filter", description = "Filter Type"),
                    @OpenApiParam(name = "input", description = "Filter Input"),
            },
            pathParams = @OpenApiParam(name = "id", description = "Student ID", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = ClassroomListResponse.class),
                    description = "The list of classroom"
            )
    )
    private void listSpecificStudent(Context ctx) {
        list(ctx, false, true, false, true);
    }

    @OpenApi(
            path = "/classroom/homeroom",
            methods = HttpMethod.GET,
            summary = "Get homeroom classroom list of teacher. Roles: TEACHER",
            description = "Get homeroom classroom list of teacher. Roles: TEACHER",
            tags = "Classroom",
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size"),
                    @OpenApiParam(name = "filter", description = "Filter Type"),
                    @OpenApiParam(name = "input", description = "Filter Input"),
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    content = @OpenApiContent(from = ClassroomListResponse.class),
                    description = "The list of classroom"
            )
    )
    private void listHomeroom(Context ctx) {
        list(ctx, false, false, true, false);
    }

    @OpenApi(
            path = "/classroom/{id}",
            methods = HttpMethod.GET,
            summary = "Get class. Roles: STAFF, TEACHER, STUDENT, ADMIN",
            description = "Get class. Roles: STAFF, TEACHER, STUDENT, ADMIN",
            tags = "Classroom",
            pathParams = @OpenApiParam(name = "id", description = "Classroom ID", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = ClassroomResponse.class),
                            description = "The class"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = ClassroomResponse.class),
                            description = "The class is not found"
                    )
            }
    )
    private void get(Context ctx) {
        long classId = Long.parseLong(ctx.pathParam("id"));
        try (var session = sessionFactory.openSession()) {
            Classroom classroom = session.get(Classroom.class, classId);
            if (classroom == null) {
                ctx.status(404);
                ctx.json(new ClassroomResponse(1, "Classroom not found", null));
                return;
            }
            ctx.json(new ClassroomResponse(0, "Get classroom", ClassroomOutput.fromEntity(classroom, id -> Profile.getOrDefault(session, id))));
        }
    }

    @OpenApi(
            path = "/classroom/{id}",
            methods = HttpMethod.PUT,
            summary = "Update class. Roles: STAFF",
            description = "Update class. Roles: STAFF",
            tags = "Classroom",
            pathParams = @OpenApiParam(name = "id", description = "Classroom ID", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ClassUpdate.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = Response.class),
                            description = "The class has been updated"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = Response.class),
                            description = "The homeroom teacher does not exist"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = Response.class),
                            description = "The class does not exist"
                    ),
                    @OpenApiResponse(
                            status = "403",
                            content = @OpenApiContent(from = Response.class),
                            description = "The homeroom teacher is not a teacher"
                    )
            }
    )
    private void update(Context ctx) {
        ClassUpdate input = ctx.bodyValidator(ClassUpdate.class)
                .check(ClassUpdate::validate, "Invalid data")
                .get();
        long classId = Long.parseLong(ctx.pathParam("id"));

        try (var session = sessionFactory.openSession()) {
            Classroom classroom = session.get(Classroom.class, classId);

            if (classroom == null) {
                ctx.status(404);
                ctx.json(new Response(1, "Class not found"));
                return;
            }

            Account homeroomTeacher = session.get(Account.class, input.getHomeroomTeacherId());
            if (homeroomTeacher == null) {
                ctx.status(404);
                ctx.json(new Response(2, "Homeroom teacher not found"));
                return;
            }
            if (JwtHandler.Role.getRole(homeroomTeacher.getRole()) != JwtHandler.Role.TEACHER) {
                ctx.status(403);
                ctx.json(new Response(3, "Homeroom teacher is not a teacher"));
                return;
            }

            Transaction transaction = session.beginTransaction();
            classroom.setName(input.getName());
            classroom.setGrade(input.getGrade());
            classroom.setYear(input.getYear());
            classroom.setHomeroomTeacher(homeroomTeacher);
            session.update(classroom);
            transaction.commit();

            ctx.json(new Response(0, "Class updated"));
        }
    }

    @OpenApi(
            path = "/classroom",
            methods = HttpMethod.POST,
            summary = "Create class. Roles: STAFF",
            description = "Create class. Roles: STAFF",
            tags = "Classroom",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ClassCreate.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = ClassroomResponse.class),
                            description = "The class has been created"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = ClassroomResponse.class),
                            description = "The homeroom teacher does not exist"
                    ),
                    @OpenApiResponse(
                            status = "403",
                            content = @OpenApiContent(from = ClassroomResponse.class),
                            description = "The homeroom teacher is not a teacher"
                    )
            }
    )
    private void create(Context ctx) {
        ClassCreate input = ctx.bodyValidator(ClassCreate.class)
                .check(ClassCreate::validate, "Invalid data")
                .get();
        try (var session = sessionFactory.openSession()) {
            Account homeroomTeacher = session.get(Account.class, input.getHomeroomTeacherId());
            if (homeroomTeacher == null) {
                ctx.status(404);
                ctx.json(new ClassroomResponse(2, "Homeroom teacher not found", null));
                return;
            }
            if (JwtHandler.Role.getRole(homeroomTeacher.getRole()) != JwtHandler.Role.TEACHER) {
                ctx.status(403);
                ctx.json(new ClassroomResponse(3, "Homeroom teacher is not a teacher", null));
                return;
            }

            Transaction transaction = session.beginTransaction();
            var classroom = new Classroom();
            classroom.setName(input.getName());
            classroom.setGrade(input.getGrade());
            classroom.setYear(input.getYear());
            classroom.setHomeroomTeacher(homeroomTeacher);
            session.save(classroom);
            transaction.commit();
            var output = ClassroomOutput.fromEntity(classroom, id -> Profile.getOrDefault(session, id));
            ctx.json(new ClassroomResponse(0, "Class created", output));
        }
    }

    @OpenApi(
            path = "/classroom/{id}/student",
            methods = HttpMethod.GET,
            summary = "Get list of students of a class. Roles: STAFF, TEACHER, ADMIN",
            description = "Get list of students of a class. Roles: STAFF, TEACHER, ADMIN",
            tags = "Classroom",
            pathParams = @OpenApiParam(name = "id", description = "Classroom ID", required = true),
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size")
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = AccountWithStudentProfileListResponse.class),
                            description = "The list of students"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = AccountWithStudentProfileListResponse.class),
                            description = "Classroom not found"
                    )
            }
    )
    private void studentList(Context ctx) {
        var paginationParameter = PaginationParameter.fromQuery(ctx);
        long classroomId = Long.parseLong(ctx.pathParam("id"));
        try (var session = sessionFactory.openSession()) {
            var classroom = session.get(Classroom.class, classroomId);
            if (classroom == null) {
                ctx.status(404);
                ctx.json(new AccountWithStudentProfileListResponse(1, "Classroom not found", null, null));
                return;
            }
            var classStudents = classroom.getStudents();
            var pagedPair = PaginationUtil.getPagedList(classStudents, paginationParameter);
            List<AccountWithStudentProfileOutput> list = new ArrayList<>();
            for (var classStudent : pagedPair.getKey()) {
                Student student = classStudent.getStudent();
                list.add(AccountWithStudentProfileOutput.fromEntity(student, id -> Profile.getOrDefault(session, id)));
            }
            ctx.json(new AccountWithStudentProfileListResponse(0, "Get student list", pagedPair.getValue(), list));
        }
    }

    @OpenApi(
            path = "/classroom/{id}/teacher",
            methods = HttpMethod.GET,
            summary = "Get list of teachers of a class. Roles: STAFF, TEACHER, STUDENT, ADMIN",
            description = "Get list of teachers of a class. Roles: STAFF, TEACHER, STUDENT, ADMIN",
            tags = "Classroom",
            pathParams = @OpenApiParam(name = "id", description = "Classroom ID", required = true),
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size")
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = TeacherWithSubjectListResponse.class),
                            description = "The list of teacher"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = TeacherWithSubjectListResponse.class),
                            description = "Classroom not found"
                    )
            }
    )
    private void teacherList(Context ctx) {
        var paginationParameter = PaginationParameter.fromQuery(ctx);
        long classroomId = Long.parseLong(ctx.pathParam("id"));
        try (var session = sessionFactory.openSession()) {
            var classroom = session.get(Classroom.class, classroomId);
            if (classroom == null) {
                ctx.status(404);
                ctx.json(new TeacherWithSubjectListResponse(1, "Classroom not found", null, null));
                return;
            }
            var classTeachers = classroom.getTeachers();
            var pagedPair = PaginationUtil.getPagedList(classTeachers, paginationParameter);
            List<TeacherWithSubjectOutput> list = new ArrayList<>();
            for (var classTeacher : pagedPair.getKey()) {
                list.add(TeacherWithSubjectOutput.fromEntity(classTeacher, id -> Profile.getOrDefault(session, id)));
            }
            ctx.json(new TeacherWithSubjectListResponse(0, "Get teacher list", pagedPair.getValue(), list));
        }
    }

    @OpenApi(
            path = "/classroom/{id}/teacher",
            methods = HttpMethod.POST,
            summary = "Add teachers to a class. Roles: STAFF",
            description = "Add teachers to a class. Roles: STAFF",
            tags = "Classroom",
            pathParams = @OpenApiParam(name = "id", description = "Classroom ID", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = TeacherWithSubjectListInput.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = TeacherWithSubjectErrorListResponse.class),
                            description = "Teachers added"
                    ),
                    @OpenApiResponse(
                            status = "400",
                            content = @OpenApiContent(from = TeacherWithSubjectErrorListResponse.class),
                            description = "Some teachers not added"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = TeacherWithSubjectErrorListResponse.class),
                            description = "Classroom not found"
                    )
            }
    )
    private void addTeacher(Context ctx) {
        var input = ctx.bodyValidator(TeacherWithSubjectListInput.class)
                .check(TeacherWithSubjectListInput::validate, "Invalid data")
                .get();
        long classroomId = Long.parseLong(ctx.pathParam("id"));

        try (var session = sessionFactory.openSession()) {
            var classroom = session.get(Classroom.class, classroomId);
            if (classroom == null) {
                ctx.status(404);
                ctx.json(new TeacherWithSubjectErrorListResponse(1, "Classroom not found", Collections.emptyList()));
                return;
            }
            Transaction transaction = session.beginTransaction();
            List<TeacherWithSubjectErrorListResponse.ErrorData> errors = new ArrayList<>();
            for (var teacherWithSubject : input.getTeachers()) {
                var teacher = session.get(Account.class, teacherWithSubject.getTeacherId());
                if (teacher == null) {
                    errors.add(new TeacherWithSubjectErrorListResponse.ErrorData(1, "Teacher not found", teacherWithSubject));
                    continue;
                }
                if (JwtHandler.Role.getRole(teacher.getRole()) != JwtHandler.Role.TEACHER) {
                    errors.add(new TeacherWithSubjectErrorListResponse.ErrorData(2, "Teacher is not a teacher", teacherWithSubject));
                    continue;
                }
                var subject = SubjectManager.getSubject(teacherWithSubject.getSubjectId());
                if (subject == null) {
                    errors.add(new TeacherWithSubjectErrorListResponse.ErrorData(3, "Subject not found", teacherWithSubject));
                    continue;
                }
                if (session.createNamedQuery("ClassTeacher.findByClassroomAndTeacherAndSubject", ClassTeacher.class)
                        .setParameter("classroomId", classroomId)
                        .setParameter("teacherId", teacherWithSubject.getTeacherId())
                        .setParameter("subjectId", teacherWithSubject.getSubjectId())
                        .uniqueResultOptional().isPresent()) {
                    errors.add(new TeacherWithSubjectErrorListResponse.ErrorData(4, "Teacher already in class", teacherWithSubject));
                    continue;
                }

                var classTeacher = new ClassTeacher();
                classTeacher.setClassroom(classroom);
                classTeacher.setTeacher(teacher);
                classTeacher.setSubjectId(subject.getId());
                session.save(classTeacher);
            }
            if (errors.isEmpty()) {
                transaction.commit();
                ctx.json(new TeacherWithSubjectErrorListResponse(0, "Teachers added", Collections.emptyList()));
            } else {
                transaction.rollback();
                ctx.status(400);
                ctx.json(new TeacherWithSubjectErrorListResponse(2, "Some teachers not added", errors));
            }
        }
    }

    @OpenApi(
            path = "/classroom/{id}/teacher",
            methods = HttpMethod.DELETE,
            summary = "Remove teachers from a class. Roles: STAFF",
            description = "Remove teachers from a class. Roles: STAFF",
            tags = "Classroom",
            pathParams = @OpenApiParam(name = "id", description = "Classroom ID", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = TeacherWithSubjectListInput.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = Response.class),
                            description = "Teachers removed"
                    )
            }
    )
    private void removeTeacher(Context ctx) {
        var input = ctx.bodyValidator(TeacherWithSubjectListInput.class)
                .check(TeacherWithSubjectListInput::validate, "Invalid data")
                .get();
        long classroomId = Long.parseLong(ctx.pathParam("id"));
        try (var session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            for (var teacherWithSubject : input.getTeachers()) {
                session.createNamedQuery("ClassTeacher.findByClassroomAndTeacherAndSubject", ClassTeacher.class)
                        .setParameter("classroomId", classroomId)
                        .setParameter("teacherId", teacherWithSubject.getTeacherId())
                        .setParameter("subjectId", teacherWithSubject.getSubjectId())
                        .stream()
                        .forEach(session::delete);
            }
            transaction.commit();
            ctx.json(new Response(0, "Teachers removed"));
        }
    }

    @OpenApi(
            path = "/classroom/{id}/student",
            methods = HttpMethod.POST,
            summary = "Add students to a class. Roles: STAFF",
            description = "Add students to a class. Roles: STAFF",
            tags = "Classroom",
            pathParams = @OpenApiParam(name = "id", description = "Classroom ID", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AccountListInput.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = AccountErrorListResponse.class),
                            description = "Students added"
                    ),
                    @OpenApiResponse(
                            status = "400",
                            content = @OpenApiContent(from = AccountErrorListResponse.class),
                            description = "Some students not added"
                    ),
                    @OpenApiResponse(
                            status = "404",
                            content = @OpenApiContent(from = AccountErrorListResponse.class),
                            description = "Classroom not found"
                    )
            }
    )
    private void addStudent(Context ctx) {
        var input = ctx.bodyValidator(AccountListInput.class)
                .check(AccountListInput::validate, "Invalid data")
                .get();
        long classroomId = Long.parseLong(ctx.pathParam("id"));

        try (var session = sessionFactory.openSession()) {
            var classroom = session.get(Classroom.class, classroomId);
            if (classroom == null) {
                ctx.status(404);
                ctx.json(new AccountErrorListResponse(1, "Classroom not found", Collections.emptyList()));
                return;
            }
            Transaction transaction = session.beginTransaction();
            List<AccountErrorListResponse.ErrorData> errors = new ArrayList<>();
            for (var accountId : input.getAccounts()) {
                var student = session.get(Student.class, accountId);
                if (student == null) {
                    errors.add(new AccountErrorListResponse.ErrorData(1, "Student not found", accountId));
                    continue;
                }
                if (session.createNamedQuery("ClassStudent.findByClassroomAndStudent", ClassStudent.class)
                        .setParameter("classroomId", classroomId)
                        .setParameter("studentId", accountId)
                        .uniqueResultOptional().isPresent()) {
                    errors.add(new AccountErrorListResponse.ErrorData(2, "Student already in class", accountId));
                    continue;
                }
                if (mainConfig.isOneClassPerYear() && session.createNamedQuery("ClassStudent.findByYearAndStudent", ClassStudent.class)
                        .setParameter("year", classroom.getYear())
                        .setParameter("studentId", accountId)
                        .uniqueResultOptional().isPresent()) {
                    errors.add(new AccountErrorListResponse.ErrorData(3, "Student already in a class on this year", accountId));
                }

                var classStudent = new ClassStudent();
                classStudent.setClassroom(classroom);
                classStudent.setStudent(student);
                session.save(classStudent);

                RecordHandler.getOrCreateRecord(session, student, classroom);
            }
            if (errors.isEmpty()) {
                transaction.commit();
                ctx.json(new AccountErrorListResponse(0, "Students added", Collections.emptyList()));
            } else {
                transaction.rollback();
                ctx.status(400);
                ctx.json(new AccountErrorListResponse(2, "Some students not added", errors));
            }
        }
    }

    @OpenApi(
            path = "/classroom/{id}/student",
            methods = HttpMethod.DELETE,
            summary = "Remove students from a class. Roles: STAFF",
            description = "Remove students from a class. Roles: STAFF",
            tags = "Classroom",
            pathParams = @OpenApiParam(name = "id", description = "Classroom ID", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AccountListInput.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = Response.class),
                            description = "Students removed"
                    )
            }
    )
    private void removeStudent(Context ctx) {
        var input = ctx.bodyValidator(AccountListInput.class)
                .check(AccountListInput::validate, "Invalid data")
                .get();
        long classroomId = Long.parseLong(ctx.pathParam("id"));

        try (var session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            for (var accountId : input.getAccounts()) {
                var classStudentQuery = session.createNamedQuery("ClassStudent.findByClassroomAndStudent", ClassStudent.class)
                        .setParameter("classroomId", classroomId)
                        .setParameter("studentId", accountId);

                var classStudentList = classStudentQuery.list();
                for (var classStudent : classStudentList) {
                    session.delete(classStudent);
                }

                var recordQuery = session.createNamedQuery("Record.findByStudentAndClassroom")
                        .setParameter("studentId", accountId)
                        .setParameter("classroomId", classroomId);
                var recordList = recordQuery.list();
                for (var record : recordList) {
                    session.delete(record);
                }
            }
            transaction.commit();
            ctx.json(new Response(0, "Students removed"));
        }
    }
}
