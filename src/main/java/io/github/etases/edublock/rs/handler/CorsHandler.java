package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.javalin.config.JavalinConfig;
import io.javalin.plugin.bundled.CorsContainer;
import io.javalin.plugin.bundled.CorsPlugin;

public class CorsHandler extends SimpleServerHandler {
    private final MainConfig mainConfig;

    @Inject
    public CorsHandler(ServerBuilder serverBuilder, MainConfig mainConfig) {
        super(serverBuilder);
        this.mainConfig = mainConfig;
    }

    @Override
    protected void setupConfig(JavalinConfig config) {
        var corsContainer = new CorsContainer();
        corsContainer.add(
                corsPluginConfig -> {
                    if (mainConfig.getServerProperties().bypassCors()) {
                        corsPluginConfig.allowCredentials = true;
                        corsPluginConfig.reflectClientOrigin = true;
                    } else {
                        var origins = mainConfig.getServerProperties().allowedOrigins();
                        if (!origins.isEmpty()) {
                            corsPluginConfig.allowHost(origins.get(0), origins.subList(1, origins.size()).toArray(new String[0]));
                        }
                    }
                }
        );
        config.plugins.register(new CorsPlugin(corsContainer.corsConfigs()));
    }
}
