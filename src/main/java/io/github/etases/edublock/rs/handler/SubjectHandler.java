package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.internal.subject.SubjectManager;
import io.github.etases.edublock.rs.model.output.SubjectListResponse;
import io.github.etases.edublock.rs.model.output.SubjectResponse;
import io.github.etases.edublock.rs.model.output.element.SubjectOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;

public class SubjectHandler extends SimpleServerHandler {
    @Inject
    public SubjectHandler(ServerBuilder serverBuilder) {
        super(serverBuilder);
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/subject/list", this::list);
        server.get("/subject/{id}", this::get);
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
        var subjectOutputs = SubjectManager.getSubjects().stream().map(SubjectOutput::fromInternal).toList();
        context.json(new SubjectListResponse(0, "Get subject list", subjectOutputs));
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
        var id = Long.parseLong(context.pathParam("id"));
        var subject = SubjectManager.getSubject(id);
        if (subject == null) {
            context.status(404);
            context.json(new SubjectResponse(1, "Subject not found", null));
            return;
        }
        context.json(new SubjectResponse(0, "Get subject", SubjectOutput.fromInternal(subject)));
    }
}
