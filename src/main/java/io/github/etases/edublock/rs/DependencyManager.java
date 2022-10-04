package io.github.etases.edublock.rs;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.github.etases.edublock.rs.internal.dependency.ServerInstanceModule;
import lombok.Getter;

import java.util.List;

/**
 * The dependency manager, which stores all dependencies
 */
public class DependencyManager {
    /**
     * The injector
     */
    @Getter
    private final Injector injector;
    private final RequestServer requestServer;

    DependencyManager(RequestServer requestServer) {
        this.requestServer = requestServer;
        injector = Guice.createInjector(getModules());
    }

    private List<Module> getModules() {
        return List.of(
                new ServerInstanceModule(requestServer)
        );
    }
}
