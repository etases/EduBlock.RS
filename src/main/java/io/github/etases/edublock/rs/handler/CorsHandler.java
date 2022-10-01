package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;

public class CorsHandler implements ServerHandler {
    @Inject
    private MainConfig mainConfig;
    @Inject
    private ServerBuilder serverBuilder;

    @Override
    public void setup() {
        serverBuilder.addConfig(config -> {
            if (mainConfig.getServerProperties().bypassCors()) {
                config.enableCorsForAllOrigins();
            } else {
                config.enableCorsForOrigin(mainConfig.getServerProperties().allowedOrigins().toArray(new String[0]));
            }
            if (mainConfig.getServerProperties().devLogging()) {
                config.enableDevLogging();
            }
        });
    }
}
