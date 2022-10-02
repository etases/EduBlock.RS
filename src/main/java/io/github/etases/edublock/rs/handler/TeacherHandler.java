package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.*;
import io.github.etases.edublock.rs.internal.jwt.JwtUtil;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryVerify;
import io.github.etases.edublock.rs.model.output.ClassroomListResponse;
import io.github.etases.edublock.rs.model.output.PendingRecordEntryListResponse;
import io.github.etases.edublock.rs.model.output.Response;
import io.github.etases.edublock.rs.model.output.StudentWithProfileListResponse;
import io.github.etases.edublock.rs.model.output.element.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TeacherHandler extends SimpleServerHandler {

    private final SessionFactory sessionFactory;

    @Inject
    public TeacherHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/teacher/class/list", new ClassListHandler().handler(), JwtHandler.Roles.TEACHER);
        server.get("/teacher/class/<classroomId>/student", new StudentListHandler().handler(), JwtHandler.Roles.TEACHER);
        server.get("/teacher/record/pending/list", new PendingRecordEntryListHandler(false).handler(), JwtHandler.Roles.TEACHER);
        server.get("/teacher/record/pending/list/<studentId>", new PendingRecordEntryListHandler(true).handler(), JwtHandler.Roles.TEACHER);
        server.post("/teacher/record/pending/verify", new RecordEntryVerifyHandler().handler(), JwtHandler.Roles.TEACHER);
    }

    private class ClassListHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get list of classes of the teacher");
                        operation.description("Get list of classes of the teacher");
                        operation.addTagsItem("Teacher");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", ClassroomListResponse.class, builder -> builder.description("The list of classrooms"));
        }

        @Override
        public void handle(Context ctx) {
            try (var session = sessionFactory.openSession()) {
                DecodedJWT jwt = JwtUtil.getDecodedFromContext(ctx);
                long userId = jwt.getClaim("id").asLong();
                var query = session.createNamedQuery("ClassTeacher.findByTeacher", ClassTeacher.class)
                        .setParameter("teacherId", userId);
                var classTeachers = query.getResultList();
                List<ClassroomOutput> list = new ArrayList<>();
                for (var classTeacher : classTeachers) {
                    var classroom = classTeacher.getClassroom();
                    list.add(ClassroomOutput.fromEntity(classroom, id -> Optional.ofNullable(session.get(Profile.class, id)).orElseGet(Profile::new)));
                }
                ctx.json(new ClassroomListResponse(0, "Get classroom list", list));
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
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", StudentWithProfileListResponse.class, builder -> builder.description("The list of students"))
                    .result("404", StudentWithProfileListResponse.class, builder -> builder.description("Classroom not found"));
        }

        @Override
        public void handle(Context ctx) {
            long classroomId = Long.parseLong(ctx.pathParam("classroomId"));
            try (var session = sessionFactory.openSession()) {
                var classroom = session.get(Classroom.class, classroomId);
                if (classroom == null) {
                    ctx.status(404);
                    ctx.json(new StudentWithProfileListResponse(1, "Classroom not found", null));
                    return;
                }
                var classStudents = classroom.getStudents();
                List<StudentWithProfileOutput> list = new ArrayList<>();
                for (var classStudent : classStudents) {
                    Student student = classStudent.getStudent();
                    Profile profile = session.get(Profile.class, student.getId());
                    if (profile == null) {
                        profile = new Profile();
                    }
                    list.add(new StudentWithProfileOutput(
                            StudentOutput.fromEntity(student),
                            ProfileOutput.fromEntity(profile)
                    ));
                }
                ctx.json(new StudentWithProfileListResponse(0, "Get student list", list));
            }
        }
    }

    private class PendingRecordEntryListHandler implements ContextHandler {
        private final boolean filterByStudent;

        private PendingRecordEntryListHandler(boolean filterByStudent) {
            this.filterByStudent = filterByStudent;
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get list of pending record entries");
                        operation.description("Get list of pending record entries");
                        operation.addTagsItem("Teacher");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", PendingRecordEntryListResponse.class, builder -> builder.description("The list of records"));
        }

        @Override
        public void handle(Context ctx) {
            DecodedJWT jwt = JwtUtil.getDecodedFromContext(ctx);
            long userId = jwt.getClaim("id").asLong();

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
                    list.add(PendingRecordEntryOutput.fromEntity(record, id -> Optional.ofNullable(session.get(Profile.class, id)).orElseGet(Profile::new)));
                }
                ctx.json(new PendingRecordEntryListResponse(0, "Get pending record entry list", list));
            }
        }
    }


    private class RecordEntryVerifyHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Verify a record entry");
                        operation.description("Verify a record entry");
                        operation.addTagsItem("Teacher");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", Response.class, builder -> builder.description("Record verified"))
                    .result("404", Response.class, builder -> builder.description("Record not found"))
                    .result("403", Response.class, builder -> builder.description("Not authorized"));
        }

        @Override
        public void handle(Context ctx) {
            PendingRecordEntryVerify input = ctx.bodyValidator(PendingRecordEntryVerify.class)
                    .check(PendingRecordEntryVerify::validate, "Invalid Record Entry id")
                    .get();

            DecodedJWT jwt = JwtUtil.getDecodedFromContext(ctx);
            long userId = jwt.getClaim("id").asLong();

            try (var session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                var account = session.get(Account.class, userId);
                var pendingRecordEntry = session.get(PendingRecordEntry.class, input.id());
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

}
