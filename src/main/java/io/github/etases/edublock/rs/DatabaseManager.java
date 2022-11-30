package io.github.etases.edublock.rs;

import io.github.etases.edublock.rs.entity.Record;
import io.github.etases.edublock.rs.entity.*;
import io.github.etases.edublock.rs.internal.property.DatabaseProperties;
import lombok.Getter;
import me.hsgamer.hscore.database.Driver;
import me.hsgamer.hscore.database.Setting;
import me.hsgamer.hscore.database.client.hibernate.HibernateClient;
import me.hsgamer.hscore.database.driver.h2.H2LocalDriver;
import me.hsgamer.hscore.database.driver.h2.H2MemoryDriver;
import me.hsgamer.hscore.database.driver.h2.H2ServerDriver;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;

import java.io.File;
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

        DatabaseProperties databaseProperties = requestServer.getMainConfig().getDatabaseProperties();
        Driver driver;
        if (databaseProperties.isMemory()) {
            driver = new H2MemoryDriver();
        } else if (databaseProperties.isFile()) {
            driver = new H2LocalDriver(new File("db"));
        } else {
            driver = new H2ServerDriver(databaseProperties.isSSH());
        }
        Setting setting = Setting.create(driver)
                .setDatabaseName(databaseProperties.name())
                .setUsername(databaseProperties.username())
                .setPassword(databaseProperties.password())
                .setHost(databaseProperties.host())
                .setPort(databaseProperties.port())
                .setClientProperty(AvailableSettings.DIALECT, H2Dialect.class.getName())
                .setClientProperty(AvailableSettings.HBM2DDL_AUTO, "update");

        if (requestServer.getMainConfig().getServerProperties().devMode()) {
            setting
                    .setClientProperty(AvailableSettings.SHOW_SQL, true)
                    .setClientProperty(AvailableSettings.FORMAT_SQL, true);
        }

        HibernateClient client = new HibernateClient(setting, driver);
        getEntityClasses().forEach(client::addEntityClass);
        sessionFactory = client.buildSessionFactory();
    }

    private List<Class<?>> getEntityClasses() {
        return List.of(
                Account.class,
                Classroom.class,
                ClassStudent.class,
                ClassTeacher.class,
                PendingRecordEntry.class,
                Profile.class,
                Record.class,
                RecordEntry.class,
                Student.class
        );
    }
}
