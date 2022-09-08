package io.github.etases.edublock.rs.database;

import lombok.Getter;
import me.hsgamer.hscore.database.Driver;
import me.hsgamer.hscore.database.Setting;
import me.hsgamer.hscore.database.client.hibernate.HibernateClient;
import me.hsgamer.hscore.database.driver.h2.H2MemoryDriver;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;

/**
 * The database session factory
 */
@Getter
public class DatabaseSessionFactory {
    /**
     * The session factory
     */
    private final SessionFactory sessionFactory;

    public DatabaseSessionFactory() {
        System.setProperty("org.jboss.logging.provider", "slf4j");

        Driver driver = new H2MemoryDriver();
        HibernateClient client = new HibernateClient(
                Setting.create(driver)
                        .setClientProperty(AvailableSettings.DIALECT, H2Dialect.class.getName()),
                driver
        );
        sessionFactory = client.buildSessionFactory();
    }
}
