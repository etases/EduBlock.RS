package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.entity.Classroom;
import io.github.etases.edublock.rs.entity.PendingRecordEntry;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.RecordEntry;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryVerify;
import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import io.github.etases.edublock.rs.model.output.element.PendingRecordEntryOutput;
import io.github.etases.edublock.rs.model.output.element.ProfileOutput;
import io.github.etases.edublock.rs.model.output.ResponseWithData;
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
    private final MainConfig mainConfig;

    @Inject
    public TeacherHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory, MainConfig mainConfig) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
        this.mainConfig = mainConfig;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/teacher/class/list", new ClassListHandler().handler(), JwtHandler.Roles.TEACHER);

        server.get("/teacher/student/list", new StudentListHandler().handler(), JwtHandler.Roles.TEACHER);

        server.get("/teacher/record/pending/list", new PendingRecordEntryListHandler().handler(), JwtHandler.Roles.TEACHER);

        server.put("/teacher/record/pending/verify", new RecordEntryVerifyHandler().handler(), JwtHandler.Roles.TEACHER);
    }

    private class ClassListHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", ResponseWithData.class, builder -> builder.description("The list of classrooms"));
        }

        @Override
        public void handle(Context ctx) {
            try (var session = sessionFactory.openSession()) {
                var query = session.createNamedQuery("Classroom.findAll", Classroom.class);
                var classrooms = query.getResultList();
                List<ClassroomOutput> list = new ArrayList<>();
                for (var room : classrooms) {
                    list.add(new ClassroomOutput(
                                    room.getId(),
                                    room.getName(),
                                    room.getGrade()
                            )
                    );
                }
                ctx.json(new ResponseWithData(0, "Get classroom list", classrooms));
            }
        }
    }

    private class StudentListHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
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

    private class PendingRecordEntryListHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
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

    private class RecordEntryVerifyHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", ResponseWithData.class, builder -> builder.description("Record verified"));
        }

        @Override
        public void handle(Context ctx) {

            PendingRecordEntryVerify input = ctx.bodyValidator(PendingRecordEntryVerify.class)
                    .check(PendingRecordEntryVerify::validate, "Invalid Record Entry id")
                    .get();

            try (var session = sessionFactory.openSession()) {
                List<ResponseWithData<PendingRecordEntryVerify>> errors = new ArrayList<>();

                Transaction transaction = session.beginTransaction();
                var record = session.get(PendingRecordEntry.class, input.id());


                if (record == null) {
                    errors.add(new ResponseWithData<>(1, "Record does not exist", input));
                } else {
                    if (input.verifyValue()) {
                        var newrecord = new RecordEntry();
                        newrecord.setSubject(record.getSubject());
                        newrecord.setFirstHalfScore(record.getFirstHalfScore());
                        newrecord.setSecondHalfScore(record.getSecondHalfScore());
                        newrecord.setFinalScore(record.getFinalScore());
                        newrecord.setTeacher(record.getTeacher());
                        newrecord.setRecord(record.getRecord());
                        session.save(newrecord);
                    }
                    session.delete(record);

                }
                if (errors.isEmpty()) {
                    transaction.commit();
                    ctx.json(new ResponseWithData(0, "Record verified successfully", input));
                } else {
                    transaction.rollback();
                    ctx.status(400);
                    ctx.json(new ResponseWithData(1, "There are errors in the record list", errors));
                }
            }

        }
    }

}
