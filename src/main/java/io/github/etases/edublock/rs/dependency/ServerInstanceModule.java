package io.github.etases.edublock.rs.dependency;

import com.google.inject.AbstractModule;
import io.github.etases.edublock.rs.RequestServer;
import lombok.RequiredArgsConstructor;

/**
 * A module for Guice to provide the {@link RequestServer}
 */
@RequiredArgsConstructor
public class ServerInstanceModule extends AbstractModule {
    private final RequestServer requestServer;

    @Override
    protected void configure() {
        bind(RequestServer.class).toInstance(requestServer);
    }
}
