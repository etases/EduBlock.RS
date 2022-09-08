package io.github.etases.edublock.rs;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.github.etases.edublock.rs.dependency.DatabaseSessionFactoryModule;
import lombok.Getter;

import java.util.List;

public class DependencyManager {
    @Getter
    private final Injector injector;

    DependencyManager() {
        injector = Guice.createInjector(getModules());
    }

    private List<Module> getModules() {
        return List.of(
                new DatabaseSessionFactoryModule()
        );
    }
}
