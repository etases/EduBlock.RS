package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.javalin.config.JavalinConfig;

public class DevHandler extends SimpleServerHandler {
    private final MainConfig mainConfig;

    @Inject
    public DevHandler(ServerBuilder serverBuilder, MainConfig mainConfig) {
        super(serverBuilder);
        this.mainConfig = mainConfig;
    }

    @Override
    protected void setupConfig(JavalinConfig config) {
        if (mainConfig.getServerProperties().devMode()) {
            config.plugins.enableDevLogging();
        }
    }
}
