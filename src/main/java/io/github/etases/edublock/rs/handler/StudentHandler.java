package io.github.etases.edublock.rs.handler;

import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.Classroom;
import io.github.etases.edublock.rs.entity.PendingRecordEntry;
import io.github.etases.edublock.rs.entity.Student;
import io.github.etases.edublock.rs.entity.Subject;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryInput;
import io.github.etases.edublock.rs.model.output.Response;
import io.github.etases.edublock.rs.model.output.StudentRequestValidationResponse;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.inject.Inject;

public class StudentHandler extends SimpleServerHandler {

    private final SessionFactory sessionFactory;

    @Inject
    public StudentHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.post("/record/request", new StudentRequestRecordValidation().handler(), JwtHandler.Roles.STUDENT);
    }

    private class StudentRequestRecordValidation implements ContextHandler {

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", StudentRequestValidationResponse.class, builder -> builder.description("Student request record validation"));
        }

        @Override
        public void handle(Context ctx) {
            PendingRecordEntryInput input = ctx.bodyValidator(PendingRecordEntryInput.class).check(PendingRecordEntryInput::validate, "Invalid data").get();

            try (var session = sessionFactory.openSession()) {

                Subject subject = session.get(Subject.class, input.subjectId());
                Student student = session.get(Student.class, input.studentId());
                Classroom classroom = session.get(Classroom.class, input.classroomId());

                if (subject == null) {
                    ctx.status(404);
                    ctx.json(new Response(1, "Subject not found"));
                    return;
                }
                if (student == null) {
                    ctx.status(404);
                    ctx.json(new Response(1, "Student not found"));
                    return;
                }
                if (classroom == null) {
                    ctx.status(404);
                    ctx.json(new Response(1, "Classroom not found"));
                    return;
                }

                Transaction transaction = session.beginTransaction();
                var pending = new PendingRecordEntry();

                pending.setSubject(subject);
                pending.setFirstHalfScore(input.firstHalfScore());
                pending.setSecondHalfScore(input.secondHalfScore());
                pending.setFinalScore(input.finalScore());

                // TEMP
                // Future change
                pending.setRecord(null);
                pending.setTeacher(null);

                session.save(input);
                transaction.commit();

                ctx.json(new Response(0, "Record validation requested"));
            }
        }
    }
}
