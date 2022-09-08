package io.github.etases.edublock.rs.dependency;

import com.google.inject.AbstractModule;
import io.github.etases.edublock.rs.database.DatabaseSessionFactory;

/**
 * A module for Guice to provide the {@link DatabaseSessionFactory}
 */
public class DatabaseSessionFactoryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DatabaseSessionFactory.class).asEagerSingleton();
    }
}
