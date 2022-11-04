package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.internal.classification.ClassificationManager;
import io.github.etases.edublock.rs.model.output.ClassificationListResponse;
import io.github.etases.edublock.rs.model.output.ClassificationResponse;
import io.github.etases.edublock.rs.model.output.element.ClassificationOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;

public class ClassificationHandler extends SimpleServerHandler {
    @Inject
    public ClassificationHandler(ServerBuilder serverBuilder) {
        super(serverBuilder);
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/classification/list", this::list);
        server.get("/classification/{id}", this::get);
    }

    @OpenApi(
            path = "/classification/list",
            methods = HttpMethod.GET,
            summary = "Get classification list",
            description = "Get classification list",
            tags = "Classification",
            responses = @OpenApiResponse(
                    status = "200",
                    description = "The list of classification",
                    content = @OpenApiContent(from = ClassificationListResponse.class)
            )
    )
    private void list(Context ctx) {
        var classificationOutputs = ClassificationManager.getClassifications().stream().map(ClassificationOutput::fromInternal).toList();
        ctx.json(new ClassificationListResponse(0, "Get classification list", classificationOutputs));
    }

    @OpenApi(
            path = "/classification/{id}",
            methods = HttpMethod.GET,
            summary = "Get classification",
            description = "Get classification",
            tags = "Classification",
            pathParams = @OpenApiParam(name = "id", description = "The classification identifier", required = true),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The classification",
                            content = @OpenApiContent(from = ClassificationResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Classification not found",
                            content = @OpenApiContent(from = ClassificationResponse.class)
                    ),
            }
    )
    private void get(Context ctx) {
        var id = ctx.pathParam("id");
        var classification = ClassificationManager.getClassification(id);
        if (classification == null) {
            ctx.status(404);
            ctx.json(new ClassificationResponse(404, "Classification not found", null));
        } else {
            ctx.json(new ClassificationResponse(0, "Get classification", ClassificationOutput.fromInternal(classification)));
        }
    }
}
