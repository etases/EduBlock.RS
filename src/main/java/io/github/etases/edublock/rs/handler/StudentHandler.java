package io.github.etases.edublock.rs.handler;

import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.RecordEntry;
import io.github.etases.edublock.rs.model.input.PendingRecordEntry;
import io.github.etases.edublock.rs.model.output.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class StudentHandler extends SimpleServerHandler {

    private final SessionFactory sessionFactory;

    @Inject
    public StudentHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.post("/student/requestValidation", new StudentRequestRecordValidation().handler(), JwtHandler.Roles.STUDENT);
    }

    private class StudentRequestRecordValidation implements ContextHandler {

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", StudentRequestValidationResponse.class, builder -> builder.description("Student request validation"));
        }

        @Override
        public void handle(Context ctx) {
            PendingRecordEntry input = ctx.bodyValidator(PendingRecordEntry.class).check(PendingRecordEntry::validate, "Invalid data").get();

            try(var session = sessionFactory.openSession()){
                var query = session.createNamedQuery("PendingRecordEntry.request", RecordEntry.class);
                var RecordEntries = query.getResultList();
                List<PendingRecordEntryOutPut> list = new ArrayList<>();

                for (var recordEntry : RecordEntries){
                    list.add(new PendingRecordEntryOutPut(
                            recordEntry.getFirstHalfScore(),
                            recordEntry.getSecondHalfScore(),
                            recordEntry.getFinalScore()
                    ));
                }
                ctx.json(new PendingRecordEntryListResponse(0, "Get classroom list", null));
        }
    }
}
}
