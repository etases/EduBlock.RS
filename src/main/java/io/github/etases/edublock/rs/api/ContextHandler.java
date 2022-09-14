package io.github.etases.edublock.rs.api;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;

public interface ContextHandler {
    default OpenApiDocumentation document() {
        return null;
    }

    void handle(Context ctx);

    default Handler handler() {
        OpenApiDocumentation documentation = document();
        if (documentation == null) {
            return this::handle;
        }
        return OpenApiBuilder.documented(documentation, this::handle);
    }
}
