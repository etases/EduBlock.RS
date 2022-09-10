package io.github.etases.edublock.rs.dependency;

import com.google.inject.AbstractModule;
import io.github.etases.edublock.rs.database.DatabaseSessionFactory;
import org.hibernate.SessionFactory;

/**
 * A module for Guice to provide the {@link DatabaseSessionFactory}
 */
public class DatabaseSessionFactoryModule extends AbstractModule {
    @Override
    protected void configure() {
        DatabaseSessionFactory sessionFactory = new DatabaseSessionFactory();
        bind(DatabaseSessionFactory.class).toInstance(sessionFactory);
        bind(SessionFactory.class).toInstance(sessionFactory.getSessionFactory());
    }
}
