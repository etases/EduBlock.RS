package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.javalin.plugin.bundled.CorsContainer;
import io.javalin.plugin.bundled.CorsPlugin;

public class CorsHandler implements ServerHandler {
    @Inject
    private MainConfig mainConfig;
    @Inject
    private ServerBuilder serverBuilder;

    @Override
    public void setup() {
        var corsContainer = new CorsContainer();
        corsContainer.add(
                corsPluginConfig -> {
                    if (mainConfig.getServerProperties().bypassCors()) {
                        corsPluginConfig.anyHost();
                    } else {
                        var origins = mainConfig.getServerProperties().allowedOrigins();
                        if (!origins.isEmpty()) {
                            corsPluginConfig.allowHost(origins.get(0), origins.subList(1, origins.size()).toArray(new String[0]));
                        }
                    }
                }
        );
        serverBuilder.addConfig(config -> {
            config.plugins.register(new CorsPlugin(corsContainer.corsConfigs()));
            if (mainConfig.getServerProperties().devLogging()) {
                config.plugins.enableDevLogging();
            }
        });
    }
}
