package io.github.etases.edublock.rs.dependency;

import com.google.inject.AbstractModule;
import io.github.etases.edublock.rs.CommandManager;
import io.github.etases.edublock.rs.DatabaseManager;
import io.github.etases.edublock.rs.RequestServer;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.config.MainConfig;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;

/**
 * A module for Guice to provide the {@link RequestServer} and its instance.
 */
@RequiredArgsConstructor
public class ServerInstanceModule extends AbstractModule {
    private final RequestServer requestServer;

    @Override
    protected void configure() {
        bind(RequestServer.class).toInstance(requestServer);
        bind(CommandManager.class).toInstance(requestServer.getCommandManager());
        bind(ServerBuilder.class).toInstance(requestServer.getServerBuilder());
        bind(MainConfig.class).toInstance(requestServer.getMainConfig());
        bind(DatabaseManager.class).toInstance(requestServer.getDatabaseManager());
        bind(SessionFactory.class).toInstance(requestServer.getDatabaseManager().getSessionFactory());
    }
}
