package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.Subject;
import io.github.etases.edublock.rs.model.input.SubjectCreateListInput;
import io.github.etases.edublock.rs.model.output.SubjectListResponse;
import io.github.etases.edublock.rs.model.output.SubjectResponse;
import io.github.etases.edublock.rs.model.output.element.SubjectOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public class SubjectHandler extends SimpleServerHandler {
    private final SessionFactory sessionFactory;

    @Inject
    public SubjectHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/subject/list", this::list);
        server.get("/subject/{id}", this::get);
        server.post("/subject/list", this::create, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
    }

    @OpenApi(
            path = "/subject/list",
            methods = HttpMethod.GET,
            summary = "Get subject list",
            description = "Get subject list",
            tags = "Subject",
            responses = @OpenApiResponse(
                    status = "200",
                    description = "The list of subjects",
                    content = @OpenApiContent(from = SubjectListResponse.class)
            )
    )
    private void list(Context context) {
        try (var session = sessionFactory.openSession()) {
            var query = session.createNamedQuery("Subject.findAll", Subject.class);
            var subjects = query.list();
            List<SubjectOutput> subjectOutputs = subjects.stream().map(SubjectOutput::fromEntity).toList();
            context.json(new SubjectListResponse(0, "Get subject list", subjectOutputs));
        }
    }

    @OpenApi(
            path = "/subject/{id}",
            methods = HttpMethod.GET,
            summary = "Get subject",
            description = "Get subject",
            tags = "Subject",
            pathParams = @OpenApiParam(name = "id", description = "The subject id", required = true),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The subject",
                            content = @OpenApiContent(from = SubjectResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Subject not found",
                            content = @OpenApiContent(from = SubjectResponse.class)
                    ),
            }
    )
    private void get(Context context) {
        try (var session = sessionFactory.openSession()) {
            var id = Long.parseLong(context.pathParam("id"));
            var subject = session.get(Subject.class, id);
            if (subject == null) {
                context.status(404);
                context.json(new SubjectResponse(1, "Subject not found", null));
                return;
            }
            context.json(new SubjectResponse(0, "Get subject", SubjectOutput.fromEntity(subject)));
        }
    }

    @OpenApi(
            path = "/subject/list",
            methods = HttpMethod.POST,
            summary = "Add subjects. Roles: ADMIN, STAFF",
            description = "Add subjects. Roles: ADMIN, STAFF",
            tags = "Subject",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = SubjectCreateListInput.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The list of created subjects",
                            content = @OpenApiContent(from = SubjectListResponse.class)
                    )
            }
    )
    private void create(Context context) {
        var input = context.bodyValidator(SubjectCreateListInput.class)
                .check(SubjectCreateListInput::validate, "Invalid subject list")
                .get();
        try (var session = sessionFactory.openSession()) {
            List<SubjectOutput> outputs = new ArrayList<>();
            var transaction = session.beginTransaction();
            for (var subjectCreate : input.getSubjects()) {
                var subject = new Subject();
                subject.setName(subjectCreate.getName());
                session.save(subject);
                outputs.add(SubjectOutput.fromEntity(subject));
            }
            transaction.commit();
            context.json(new SubjectListResponse(0, "Add subjects", outputs));
        }
    }
}
