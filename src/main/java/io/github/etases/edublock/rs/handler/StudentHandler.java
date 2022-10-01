package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.PendingRecordEntry;
import io.github.etases.edublock.rs.entity.RecordEntry;
import io.github.etases.edublock.rs.internal.jwt.JwtUtil;
import io.github.etases.edublock.rs.model.input.PendingRecordEntryInput;
import io.github.etases.edublock.rs.model.output.*;
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
                    .result("200", StudentRequestValidationResponse.class, builder -> builder.description("Student request validation"));
        }

        @Override
        public void handle(Context ctx) {
            PendingRecordEntryInput input = ctx.bodyValidator(PendingRecordEntryInput.class).check(PendingRecordEntryInput::validate, "Invalid data").get();
            DecodedJWT jwt = JwtUtil.getDecodedFromContext(ctx);
            long id = jwt.getClaim("pendingRecordEntries").asLong();
            try(var session = sessionFactory.openSession()){
                var query = session.createNamedQuery("PendingRecordEntry.request", RecordEntry.class).setParameter("id", id + "%");
                var RecordEntries = query.getResultList();
                if ( RecordEntries != null){
                    ctx.status(400);
                    ctx.json(new Response(1, "PendingRecordEntry already exists"));
                    return;
                }
                Transaction transaction = session.beginTransaction();

                var pending = new PendingRecordEntry();

                pending.setSubject(input.subject());
                pending.setFirstHalfScore(input.firstHalfScore());
                pending.setSecondHalfScore(input.secondHalfScore());
                pending.setFinalScore(input.finalScore());

                session.save(input);
                transaction.commit();
                
                ctx.json(new Response(0, "Record validation requested"));
        }
    }
}
}
