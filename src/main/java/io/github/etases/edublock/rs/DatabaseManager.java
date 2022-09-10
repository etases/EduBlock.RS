package io.github.etases.edublock.rs;

import io.github.etases.edublock.rs.entity.User;
import lombok.Getter;
import me.hsgamer.hscore.database.Driver;
import me.hsgamer.hscore.database.Setting;
import me.hsgamer.hscore.database.client.hibernate.HibernateClient;
import me.hsgamer.hscore.database.driver.h2.H2MemoryDriver;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;

import java.util.List;

/**
 * The database session factory
 */
@Getter
public class DatabaseManager {
    /**
     * The session factory
     */
    private final SessionFactory sessionFactory;

    public DatabaseManager(RequestServer requestServer) {
        System.setProperty("org.jboss.logging.provider", "slf4j");

        Driver driver = new H2MemoryDriver();
        Setting setting = Setting.create(driver)
                .setClientProperty(AvailableSettings.DIALECT, H2Dialect.class.getName())
                .setClientProperty(AvailableSettings.SHOW_SQL, "true")
                .setClientProperty(AvailableSettings.FORMAT_SQL, "true")
                .setClientProperty(AvailableSettings.HBM2DDL_AUTO, "update");

        HibernateClient client = new HibernateClient(setting, driver);
        getEntityClasses().forEach(client::addEntityClass);
        sessionFactory = client.buildSessionFactory();
    }

    private List<Class<?>> getEntityClasses() {
        return List.of(
                User.class
        );
    }
}
