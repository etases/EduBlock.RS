package io.github.etases.edublock.rs.handler;

import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
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
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
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
        server.post("/record/request", new RequestHandler().handler(), JwtHandler.Role.STUDENT, JwtHandler.Role.TEACHER);
        server.get("/record/pending/list", new ListPendingHandler(false).handler(), JwtHandler.Role.TEACHER);
        server.get("/record/pending/list/<studentId>", new ListPendingHandler(true).handler(), JwtHandler.Role.TEACHER);
        server.post("/record/pending/verify", new VerifyHandler().handler(), JwtHandler.Role.TEACHER);
        server.get("/record/<classroomId>", new GetHandler(true).handler(), JwtHandler.Role.STUDENT);
        server.get("/record/<classroomId>/<studentId>", new GetHandler(false).handler(), JwtHandler.Role.TEACHER);
    }

    private class GetHandler implements ContextHandler {
        private final boolean isOwnRecordOnly;

        private GetHandler(boolean isOwnRecordOnly) {
            this.isOwnRecordOnly = isOwnRecordOnly;
        }

        @Override
        public void handle(Context ctx) {
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

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        String description;
                        if (isOwnRecordOnly) {
                            description = "Get own record. Roles: STUDENT";
                        } else {
                            description = "Get student record. Roles: TEACHER";
                        }
                        operation.summary(description);
                        operation.description(description);
                        operation.addTagsItem("Record");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", RecordResponse.class, builder -> builder.description("The record"))
                    .result("404", RecordResponse.class, builder -> builder.description("Record not found"));
        }
    }

    private class RequestHandler implements ContextHandler {

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Request record update. Roles: STUDENT, TEACHER");
                        operation.description("Request record update. Roles: STUDENT, TEACHER");
                        operation.addTagsItem("Record");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", Response.class, builder -> builder.description("Student request record validation"));
        }

        @Override
        public void handle(Context ctx) {
            PendingRecordEntryInput input = ctx.bodyValidator(PendingRecordEntryInput.class).check(PendingRecordEntryInput::validate, "Invalid data").get();
            long userId = JwtHandler.getUserId(ctx);

            try (var session = sessionFactory.openSession()) {
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
                pending.setRequestDate(new Date());
                pending.setRecord(record);

                session.save(input);
                transaction.commit();

                ctx.json(new Response(0, "Record validation requested"));
            }
        }
    }

    private class ListPendingHandler implements ContextHandler {
        private final boolean filterByStudent;

        private ListPendingHandler(boolean filterByStudent) {
            this.filterByStudent = filterByStudent;
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get list of pending record entries. Roles: TEACHER");
                        operation.description("Get list of pending record entries. Roles: TEACHER");
                        operation.addTagsItem("Record");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", PendingRecordEntryListResponse.class, builder -> builder.description("The list of records"));
        }

        @Override
        public void handle(Context ctx) {
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
    }

    private class VerifyHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Verify a record entry. Roles: TEACHER");
                        operation.description("Verify a record entry. Roles: TEACHER");
                        operation.addTagsItem("Record");
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

            long userId = JwtHandler.getUserId(ctx);

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
