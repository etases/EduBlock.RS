package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.*;
import io.github.etases.edublock.rs.model.input.*;
import io.github.etases.edublock.rs.model.output.*;
import io.github.etases.edublock.rs.model.output.element.AccountWithStudentProfileOutput;
import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import io.github.etases.edublock.rs.model.output.element.TeacherWithSubjectOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassroomHandler extends SimpleServerHandler {
    private final SessionFactory sessionFactory;

    @Inject
    public ClassroomHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.post("/classroom", new CreateHandler().handler(), JwtHandler.Role.STAFF);
        server.get("/classroom", new ListHandler(false, false, false).handler(), JwtHandler.Role.STAFF);
        server.get("/classroom/teacher", new ListHandler(true, false, false).handler(), JwtHandler.Role.TEACHER);
        server.get("/classroom/student", new ListHandler(false, true, false).handler(), JwtHandler.Role.STUDENT);
        server.get("/classroom/homeroom", new ListHandler(false, false, true).handler(), JwtHandler.Role.TEACHER);
        server.get("/classroom/<id>", new GetHandler().handler(), JwtHandler.Role.STAFF, JwtHandler.Role.TEACHER, JwtHandler.Role.STUDENT);
        server.put("/classroom/<id>", new UpdateHandler().handler(), JwtHandler.Role.STAFF);
        server.get("/classroom/<id>/teacher", new TeacherListHandler().handler(), JwtHandler.Role.STAFF, JwtHandler.Role.TEACHER, JwtHandler.Role.STUDENT);
        server.get("/classroom/<id>/student", new StudentListHandler().handler(), JwtHandler.Role.TEACHER, JwtHandler.Role.STAFF);
        server.post("/classroom/<id>/student", new AddStudentHandler().handler(), JwtHandler.Role.STAFF);
        server.delete("/classroom/<id>/student", new RemoveStudentHandler().handler(), JwtHandler.Role.STAFF);
        server.post("/classroom/<id>/teacher", new AddTeacherHandler().handler(), JwtHandler.Role.STAFF);
        server.delete("/classroom/<id>/teacher", new RemoveTeacherHandler().handler(), JwtHandler.Role.STAFF);
    }

    private class ListHandler implements ContextHandler {
        private final boolean isTeacher;
        private final boolean isStudent;
        private final boolean isHomeroom;

        private ListHandler(boolean isTeacher, boolean isStudent, boolean isHomeroom) {
            this.isTeacher = isTeacher;
            this.isStudent = isStudent;
            this.isHomeroom = isHomeroom;
        }

        @Override
        public void handle(Context ctx) {
            try (var session = sessionFactory.openSession()) {
                List<Classroom> classrooms;
                if (isTeacher) {
                    long userId = JwtHandler.getUserId(ctx);
                    var query = session.createNamedQuery("ClassTeacher.findByTeacher", ClassTeacher.class)
                            .setParameter("teacherId", userId);
                    var classTeachers = query.getResultList();
                    classrooms = classTeachers.stream().map(ClassTeacher::getClassroom).toList();
                } else if (isStudent) {
                    long userId = JwtHandler.getUserId(ctx);
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
                List<ClassroomOutput> list = new ArrayList<>();
                for (var classroom : classrooms) {
                    list.add(ClassroomOutput.fromEntity(classroom, id -> Profile.getOrDefault(session, id)));
                }
                ctx.json(new ClassroomListResponse(0, "Get classroom list", list));
            }
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get class list" + (isHomeroom ? " (homeroom)" : ""));
                        operation.description("Get class list" + (isHomeroom ? " (homeroom)" : ""));
                        if (isTeacher || isHomeroom) {
                            operation.addTagsItem("Teacher");
                        } else if (isStudent) {
                            operation.addTagsItem("Student");
                        } else {
                            operation.addTagsItem("Staff");
                        }
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", ClassroomListResponse.class, builder -> builder.description("The list of classroom"));
        }
    }

    private class GetHandler implements ContextHandler {
        @Override
        public void handle(Context ctx) {
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

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get class");
                        operation.description("Get class");
                        operation.addTagsItem("Staff");
                        operation.addTagsItem("Teacher");
                        operation.addTagsItem("Student");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", ClassroomResponse.class, builder -> builder.description("The class"))
                    .result("404", ClassroomResponse.class, builder -> builder.description("The class is not found"));
        }
    }

    private class UpdateHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Update class");
                        operation.description("Update class");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(ClassUpdate.class)
                    .result("200", Response.class, builder -> builder.description("The class has been updated"))
                    .result("404", ClassroomResponse.class, builder -> builder.description("The homeroom teacher does not exist"))
                    .result("403", ClassroomResponse.class, builder -> builder.description("The homeroom teacher is not a teacher"));
        }

        @Override
        public void handle(Context ctx) {
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

                Account homeroomTeacher = session.get(Account.class, input.homeroomTeacherId());
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
                classroom.setName(input.name());
                classroom.setGrade(input.grade());
                classroom.setHomeroomTeacher(homeroomTeacher);
                session.update(classroom);
                transaction.commit();

                ctx.json(new Response(0, "Class updated"));
            }
        }
    }

    private class CreateHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Create class");
                        operation.description("Create class");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(ClassCreate.class, builder -> builder.description("The class to create"))
                    .result("200", ClassroomResponse.class, builder -> builder.description("The class has been created"))
                    .result("404", ClassroomResponse.class, builder -> builder.description("The homeroom teacher does not exist"))
                    .result("403", ClassroomResponse.class, builder -> builder.description("The homeroom teacher is not a teacher"));
        }

        @Override
        public void handle(Context ctx) {
            ClassCreate input = ctx.bodyValidator(ClassCreate.class)
                    .check(ClassCreate::validate, "Invalid data")
                    .get();
            try (var session = sessionFactory.openSession()) {
                Account homeroomTeacher = session.get(Account.class, input.homeroomTeacherId());
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
                classroom.setName(input.name());
                classroom.setGrade(input.grade());
                classroom.setHomeroomTeacher(homeroomTeacher);
                session.save(classroom);
                transaction.commit();
                var output = ClassroomOutput.fromEntity(classroom, id -> Profile.getOrDefault(session, id));
                ctx.json(new ClassroomResponse(0, "Class created", output));
            }
        }
    }

    private class StudentListHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get list of students of a class");
                        operation.description("Get list of students of a class");
                        operation.addTagsItem("Teacher");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", AccountWithStudentProfileListResponse.class, builder -> builder.description("The list of students"))
                    .result("404", AccountWithStudentProfileListResponse.class, builder -> builder.description("Classroom not found"));
        }

        @Override
        public void handle(Context ctx) {
            long classroomId = Long.parseLong(ctx.pathParam("id"));
            try (var session = sessionFactory.openSession()) {
                var classroom = session.get(Classroom.class, classroomId);
                if (classroom == null) {
                    ctx.status(404);
                    ctx.json(new AccountWithStudentProfileListResponse(1, "Classroom not found", null));
                    return;
                }
                var classStudents = classroom.getStudents();
                List<AccountWithStudentProfileOutput> list = new ArrayList<>();
                for (var classStudent : classStudents) {
                    Student student = classStudent.getStudent();
                    list.add(AccountWithStudentProfileOutput.fromEntity(student, id -> Profile.getOrDefault(session, id)));
                }
                ctx.json(new AccountWithStudentProfileListResponse(0, "Get student list", list));
            }
        }
    }

    private class TeacherListHandler implements ContextHandler {

        @Override
        public void handle(Context ctx) {
            long classroomId = Long.parseLong(ctx.pathParam("id"));
            try (var session = sessionFactory.openSession()) {
                var classroom = session.get(Classroom.class, classroomId);
                if (classroom == null) {
                    ctx.status(404);
                    ctx.json(new TeacherWithSubjectListResponse(1, "Classroom not found", null));
                    return;
                }
                var classTeachers = classroom.getTeachers();
                List<TeacherWithSubjectOutput> list = new ArrayList<>();
                for (var classTeacher : classTeachers) {
                    list.add(TeacherWithSubjectOutput.fromEntity(classTeacher, id -> Profile.getOrDefault(session, id)));
                }
                ctx.json(new TeacherWithSubjectListResponse(0, "Get teacher list", list));
            }
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get list of teachers of a class");
                        operation.description("Get list of teachers of a class");
                        operation.addTagsItem("Staff");
                        operation.addTagsItem("Teacher");
                        operation.addTagsItem("Student");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", TeacherWithSubjectListResponse.class, builder -> builder.description("The list of teacher"))
                    .result("404", TeacherWithSubjectListResponse.class, builder -> builder.description("Classroom not found"));
        }
    }

    private class AddTeacherHandler implements ContextHandler {

        @Override
        public void handle(Context ctx) {
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
                List<ResponseWithData<TeacherWithSubjectInput>> errors = new ArrayList<>();
                for (var teacherWithSubject : input.teachers()) {
                    var teacher = session.get(Account.class, teacherWithSubject.teacherId());
                    if (teacher == null) {
                        errors.add(new ResponseWithData<>(1, "Teacher not found", teacherWithSubject));
                        continue;
                    }
                    if (JwtHandler.Role.getRole(teacher.getRole()) != JwtHandler.Role.TEACHER) {
                        errors.add(new ResponseWithData<>(2, "Teacher is not a teacher", teacherWithSubject));
                        continue;
                    }
                    var subject = session.get(Subject.class, teacherWithSubject.subjectId());
                    if (subject == null) {
                        errors.add(new ResponseWithData<>(3, "Subject not found", teacherWithSubject));
                        continue;
                    }
                    var classTeacher = new ClassTeacher();
                    classTeacher.setClassroom(classroom);
                    classTeacher.setTeacher(teacher);
                    classTeacher.setSubject(subject);
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

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Add teachers to a class");
                        operation.description("Add teachers to a class");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(TeacherWithSubjectListInput.class, builder -> builder.description("The list of teachers to add"))
                    .result("200", TeacherWithSubjectErrorListResponse.class, builder -> builder.description("Teachers added"))
                    .result("400", TeacherWithSubjectErrorListResponse.class, builder -> builder.description("Some teachers not added"))
                    .result("404", TeacherWithSubjectErrorListResponse.class, builder -> builder.description("Classroom not found"));
        }
    }

    private class RemoveTeacherHandler implements ContextHandler {
        @Override
        public void handle(Context ctx) {
            var input = ctx.bodyValidator(TeacherWithSubjectListInput.class)
                    .check(TeacherWithSubjectListInput::validate, "Invalid data")
                    .get();
            long classroomId = Long.parseLong(ctx.pathParam("id"));
            try (var session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                for (var teacherWithSubject : input.teachers()) {
                    session.createNamedQuery("ClassTeacher.findByClassroomAndTeacherAndSubject", ClassTeacher.class)
                            .setParameter("classroomId", classroomId)
                            .setParameter("teacherId", teacherWithSubject.teacherId())
                            .setParameter("subjectId", teacherWithSubject.subjectId())
                            .uniqueResultOptional()
                            .ifPresent(session::delete);
                }
                transaction.commit();
                ctx.json(new Response(0, "Teachers removed"));
            }
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Remove teachers from a class");
                        operation.description("Remove teachers from a class");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(TeacherWithSubjectListInput.class, builder -> builder.description("The list of teachers to remove"))
                    .result("200", Response.class, builder -> builder.description("Teachers removed"));
        }
    }

    private class AddStudentHandler implements ContextHandler {
        @Override
        public void handle(Context ctx) {
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
                List<ResponseWithData<Long>> errors = new ArrayList<>();
                for (var accountId : input.accounts()) {
                    var student = session.get(Student.class, accountId);
                    if (student == null) {
                        errors.add(new ResponseWithData<>(1, "Student not found", accountId));
                        continue;
                    }
                    var classStudent = new ClassStudent();
                    classStudent.setClassroom(classroom);
                    classStudent.setStudent(student);
                    session.save(classStudent);
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

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Add students to a class");
                        operation.description("Add students to a class");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(AccountListInput.class, builder -> builder.description("The list of students to add"))
                    .result("200", AccountErrorListResponse.class, builder -> builder.description("Students added"))
                    .result("400", AccountErrorListResponse.class, builder -> builder.description("Some students not added"))
                    .result("404", AccountErrorListResponse.class, builder -> builder.description("Classroom not found"));
        }
    }

    private class RemoveStudentHandler implements ContextHandler {
        @Override
        public void handle(Context ctx) {
            var input = ctx.bodyValidator(AccountListInput.class)
                    .check(AccountListInput::validate, "Invalid data")
                    .get();
            long classroomId = Long.parseLong(ctx.pathParam("id"));

            try (var session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                for (var accountId : input.accounts()) {
                    session.createNamedQuery("ClassStudent.findByClassroomAndStudent", ClassStudent.class)
                            .setParameter("classroomId", classroomId)
                            .setParameter("studentId", accountId)
                            .uniqueResultOptional()
                            .ifPresent(session::delete);
                }
                transaction.commit();
                ctx.json(new Response(0, "Students removed"));
            }
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Remove students from a class");
                        operation.description("Remove students from a class");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(AccountListInput.class, builder -> builder.description("The list of students to remove"))
                    .result("200", Response.class, builder -> builder.description("Students removed"));
        }
    }
}
