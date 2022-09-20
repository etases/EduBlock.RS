package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.RecordEntry;
import io.github.etases.edublock.rs.internal.jwt.JwtUtil;
import io.github.etases.edublock.rs.model.input.AccountInput;
import io.github.etases.edublock.rs.model.output.RecordEntryListResponse;
import io.github.etases.edublock.rs.model.output.RecordEntryOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public class RecordEntryHandler extends SimpleServerHandler {
    private final SessionFactory sessionFactory;

    @Inject
    public RecordEntryHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/recordentry", new RecordEntryListHandler().handler(), JwtHandler.Roles.ADMIN);
    }

    private class RecordEntryListHandler implements ContextHandler {

        @Override
        public void handle(Context ctx) {
            try(var session = sessionFactory.openSession()){
                DecodedJWT jwt = JwtUtil.getDecodedFromContext(ctx);
                long userId = jwt.getClaim("id").asLong();
                var query = session.createNamedQuery("RecordEntry.findPersonalEntry", RecordEntry.class).setParameter("id", userId);
                var recordEntries = query.uniqueResult();
                RecordEntryOutput outPut = new RecordEntryOutput(recordEntries.getId(), recordEntries.getFirstHalfScore(), recordEntries.getSecondHalfScore(), recordEntries.getFinalScore());
                ctx.json(new RecordEntryListResponse(0, "Get personal recordEntry", outPut));
            }
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", RecordEntryListResponse.class, builder -> builder.description("The personal record entry"));
        }
    }
}
