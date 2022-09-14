package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.RecordEntry;
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
        server.get("/recordentry/list", new RecordEntryListHandler().handler(), JwtHandler.Roles.STUDENT);
    }

    private class RecordEntryListHandler implements ContextHandler {

        @Override
        public void handle(Context ctx) {
            try(var session = sessionFactory.openSession()){
                var query = session.createNamedQuery("RecordEntry.findAll", RecordEntry.class);
                var recordEntries = query.getResultList();
                List<RecordEntryOutput> list = new ArrayList<>();
                for(var recordEntry : recordEntries){
                    list.add(new RecordEntryOutput(
                            recordEntry.getId(),
                            recordEntry.getFirstHalfScore(),
                            recordEntry.getSecondHalfScore(),
                            recordEntry.getFinalScore()
                    ));
                }
                ctx.json(new RecordEntryListResponse(0, "Get recordEntry", list));
            }
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", RecordEntryListResponse.class, builder -> builder.description("The list of record entry"));
        }
    }
}
