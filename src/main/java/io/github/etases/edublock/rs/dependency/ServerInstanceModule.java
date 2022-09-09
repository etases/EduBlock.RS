package io.github.etases.edublock.rs.dependency;

import com.google.inject.AbstractModule;
import io.github.etases.edublock.rs.CommandManager;
import io.github.etases.edublock.rs.RequestServer;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

/**
 * A module for Guice to provide the {@link RequestServer} and its instance.
 */
@RequiredArgsConstructor
public class ServerInstanceModule extends AbstractModule {
    private final RequestServer requestServer;

    @Override
    protected void configure() {
        bind(RequestServer.class).toInstance(requestServer);
        bind(Javalin.class).toInstance(requestServer.getServer());
        bind(CommandManager.class).toInstance(requestServer.getCommandManager());
    }
}
