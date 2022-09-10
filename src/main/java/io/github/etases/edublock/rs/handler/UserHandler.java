package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.User;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import org.hibernate.SessionFactory;

public class UserHandler extends SimpleServerHandler {
    private final SessionFactory sessionFactory;

    @Inject
    public UserHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/user/<id>", OpenApiBuilder.documented(
                OpenApiBuilder.document()
                        .result("404")
                        .result("200", User.class, builder -> {
                            builder.description("The user");
                        }),
                ctx -> {
                    try (var session = sessionFactory.openSession()) {
                        var user = session.get(User.class, Long.parseLong(ctx.pathParam("id")));
                        if (user == null) {
                            ctx.status(404);
                        } else {
                            ctx.json(user);
                        }
                    }
                }
        ));
        server.post("/user", OpenApiBuilder.documented(
                OpenApiBuilder.document()
                        .body(User.class)
                        .result("200", User.class),
                ctx -> {
                    try (var session = sessionFactory.openSession()) {
                        var transaction = session.beginTransaction();
                        var user = ctx.bodyAsClass(User.class);
                        session.save(user);
                        ctx.json(user);
                        transaction.commit();
                    }
                }
        ));
    }
}
