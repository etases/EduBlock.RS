package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.ClassTeacher;
import io.github.etases.edublock.rs.entity.PendingRecordEntry;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.RecordEntry;
import io.github.etases.edublock.rs.internal.jwt.JwtUtil;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryVerify;
import io.github.etases.edublock.rs.model.output.ClassroomListResponse;
import io.github.etases.edublock.rs.model.output.Response;
import io.github.etases.edublock.rs.model.output.ResponseWithData;
import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import io.github.etases.edublock.rs.model.output.element.PendingRecordEntryOutput;
import io.github.etases.edublock.rs.model.output.element.ProfileOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

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
        server.get("/teacher/student/list", new StudentListHandler().handler(), JwtHandler.Roles.TEACHER);
        server.get("/teacher/record/pending/list", new PendingRecordEntryListHandler().handler(), JwtHandler.Roles.TEACHER);
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
                    list.add(new ClassroomOutput(
                            classroom.getId(),
                            classroom.getName(),
                            classroom.getGrade()
                    ));
                }
                ctx.json(new ClassroomListResponse(0, "Get classroom list", list));
            }
        }
    }

    // TODO: Filter by homeroom teacher & classroom
    private class StudentListHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get list of students");
                        operation.description("Get list of students");
                        operation.addTagsItem("Teacher");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", ResponseWithData.class, builder -> builder.description("The list of students"));
        }

        @Override
        public void handle(Context ctx) {
            try (var session = sessionFactory.openSession()) {
                var query = session.createNamedQuery("Profile.findAll", Profile.class);
                var students = query.getResultList();
                List<ProfileOutput> list = new ArrayList<>();
                for (var student : students) {
                    list.add(new ProfileOutput(
                                    student.getId(),
                                    student.getFirstName(),
                                    student.getLastName(),
                                    student.getPhone(),
                                    student.getBirthDate(),
                                    student.getAddress(),
                                    student.getEmail(),
                                    student.getAvatar()
                            )
                    );
                }
                ctx.json(new ResponseWithData(0, "Get classroom list", list));
            }
        }
    }

    // TODO: Filter by homeroom teacher
    private class PendingRecordEntryListHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get list of pending record entries");
                        operation.description("Get list of pending record entries");
                        operation.addTagsItem("Teacher");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", ResponseWithData.class, builder -> builder.description("The list of records"));
        }

        @Override
        public void handle(Context ctx) {
            try (var session = sessionFactory.openSession()) {
                var query = session.createNamedQuery("PendingRecordEntry.findAll", PendingRecordEntry.class);
                var records = query.getResultList();
                List<PendingRecordEntryOutput> list = new ArrayList<>();
                for (var record : records) {
                    list.add(new PendingRecordEntryOutput(
                                    record.getId(),
                                    record.getSubject(),
                                    record.getFirstHalfScore(),
                                    record.getSecondHalfScore(),
                                    record.getFinalScore(),
                                    record.getTeacher()
                            )
                    );
                }
                ctx.json(new ResponseWithData(0, "Get classroom list", list));
            }
        }
    }


    // TODO: Filter by homeroom teacher
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
                    .result("404", Response.class, builder -> builder.description("Record not found"));
        }

        @Override
        public void handle(Context ctx) {
            PendingRecordEntryVerify input = ctx.bodyValidator(PendingRecordEntryVerify.class)
                    .check(PendingRecordEntryVerify::validate, "Invalid Record Entry id")
                    .get();

            try (var session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                var pendingRecordEntry = session.get(PendingRecordEntry.class, input.id());
                if (pendingRecordEntry == null) {
                    ctx.status(404);
                    ctx.json(new Response(1, "Record not found"));
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
                    session.save(recordEntry);
                }
                session.delete(pendingRecordEntry);
                transaction.commit();
                ctx.json(new Response(0, "Record verified"));
            }
        }
    }

}
