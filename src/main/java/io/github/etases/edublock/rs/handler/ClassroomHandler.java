package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.Classroom;
import io.github.etases.edublock.rs.model.output.ClassroomListResponse;
import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public class ClassroomHandler extends SimpleServerHandler {
    private final SessionFactory sessionFactory;

    @Inject
    public ClassroomHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/classroom/list", new ClassroomListHandler().handler(), JwtHandler.Roles.STAFF);
    }

    private class ClassroomListHandler implements ContextHandler {

        @Override
        public void handle(Context ctx) {
            try (var session = sessionFactory.openSession()) {
                var query = session.createNamedQuery("Classroom.findAll", Classroom.class);
                var classrooms = query.getResultList();
                List<ClassroomOutput> list = new ArrayList<>();
                for (var classroom : classrooms) {
                    list.add(new ClassroomOutput(
                            classroom.getId(),
                            classroom.getName(),
                            classroom.getGrade()
                    ));
                }
                ctx.json(new ClassroomListResponse(0, "Get classroom list", list));
            }
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document().operation(SwaggerHandler.addSecurity())
                    .result("200", ClassroomListResponse.class, builder -> builder.description("The list of classroom"));
        }
    }
}
