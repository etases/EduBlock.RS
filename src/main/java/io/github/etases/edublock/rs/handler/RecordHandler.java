package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.Record;
import io.github.etases.edublock.rs.entity.*;
import io.github.etases.edublock.rs.internal.jwt.JwtUtil;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryInput;
import io.github.etases.edublock.rs.model.output.RecordResponse;
import io.github.etases.edublock.rs.model.output.Response;
import io.github.etases.edublock.rs.model.output.StudentRequestValidationResponse;
import io.github.etases.edublock.rs.model.output.element.RecordOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.inject.Inject;
import java.util.Optional;

public class RecordHandler extends SimpleServerHandler {

    private final SessionFactory sessionFactory;

    @Inject
    public RecordHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/record/<classroomId>", new GetRecordHandler(true).handler(), JwtHandler.Roles.STUDENT);
        server.get("/record/student/<studentId>/<classroomId>", new GetRecordHandler(false).handler(), JwtHandler.Roles.TEACHER);
        server.post("/record/request", new RequestRecordUpdateHandler().handler(), JwtHandler.Roles.STUDENT, JwtHandler.Roles.TEACHER);
    }

    private class GetRecordHandler implements ContextHandler {
        private final boolean isOwnRecordOnly;

        private GetRecordHandler(boolean isOwnRecordOnly) {
            this.isOwnRecordOnly = isOwnRecordOnly;
        }

        @Override
        public void handle(Context ctx) {
            try (var session = sessionFactory.openSession()) {
                long studentId;
                if (isOwnRecordOnly) {
                    DecodedJWT jwt = JwtUtil.getDecodedFromContext(ctx);
                    studentId = jwt.getClaim("id").asLong();
                } else {
                    studentId = Long.parseLong(ctx.pathParam("studentId"));
                }

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
                var recordOutput = RecordOutput.fromEntity(record, id -> Optional.ofNullable(session.get(Profile.class, id)).orElseGet(Profile::new));
                ctx.json(new RecordResponse(0, "Get personal record", recordOutput));
            }
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get personal record");
                        operation.description("Get personal record");
                        operation.addTagsItem("Record");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", RecordResponse.class, builder -> builder.description("The record"))
                    .result("404", RecordResponse.class, builder -> builder.description("Record not found"));
        }
    }

    private class RequestRecordUpdateHandler implements ContextHandler {

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Request record update");
                        operation.description("Request record update");
                        operation.addTagsItem("Record");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", StudentRequestValidationResponse.class, builder -> builder.description("Student request record validation"));
        }

        @Override
        public void handle(Context ctx) {
            PendingRecordEntryInput input = ctx.bodyValidator(PendingRecordEntryInput.class).check(PendingRecordEntryInput::validate, "Invalid data").get();

            try (var session = sessionFactory.openSession()) {
                DecodedJWT jwt = JwtUtil.getDecodedFromContext(ctx);
                long userId = jwt.getClaim("id").asLong();
                var requester = session.get(Account.class, userId);

                Subject subject = session.get(Subject.class, input.subjectId());
                if (subject == null) {
                    ctx.status(404);
                    ctx.json(new Response(1, "Subject not found"));
                    return;
                }

                Student student = session.get(Student.class, input.studentId());
                if (student == null) {
                    ctx.status(404);
                    ctx.json(new Response(2, "Student not found"));
                    return;
                }

                Classroom classroom = session.get(Classroom.class, input.classroomId());
                if (classroom == null) {
                    ctx.status(404);
                    ctx.json(new Response(3, "Classroom not found"));
                    return;
                }

                var classTeacherQuery = session.createNamedQuery("ClassTeacher.findByClassroomAndSubject", ClassTeacher.class)
                        .setParameter("classroomId", input.classroomId())
                        .setParameter("subjectId", input.subjectId());
                var classTeacher = classTeacherQuery.uniqueResult();
                if (classTeacher == null) {
                    ctx.status(404);
                    ctx.json(new Response(4, "ClassTeacher not found"));
                    return;
                }
                var teacher = classTeacher.getTeacher();

                var recordQuery = session.createNamedQuery("Record.findByStudentAndClassroom", Record.class)
                        .setParameter("studentId", input.studentId())
                        .setParameter("classroomId", input.classroomId());
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
                pending.setFirstHalfScore(input.firstHalfScore());
                pending.setSecondHalfScore(input.secondHalfScore());
                pending.setFinalScore(input.finalScore());
                pending.setTeacher(teacher);
                pending.setRequester(requester);
                pending.setRecord(record);

                session.save(input);
                transaction.commit();

                ctx.json(new Response(0, "Record validation requested"));
            }
        }
    }
}
