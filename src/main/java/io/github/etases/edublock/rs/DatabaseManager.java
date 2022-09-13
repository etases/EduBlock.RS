package io.github.etases.edublock.rs;

import io.github.etases.edublock.rs.entity.*;
import io.github.etases.edublock.rs.entity.Record;
import io.github.etases.edublock.rs.internal.property.DatabaseProperties;
import lombok.Getter;
import me.hsgamer.hscore.database.Driver;
import me.hsgamer.hscore.database.Setting;
import me.hsgamer.hscore.database.client.hibernate.HibernateClient;
import me.hsgamer.hscore.database.driver.h2.H2LocalDriver;
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

        DatabaseProperties databaseProperties = requestServer.getMainConfig().getDatabaseProperties();
        Driver driver = databaseProperties.isMemory() ? new H2MemoryDriver() : new H2LocalDriver();
        Setting setting = Setting.create(driver)
                .setDatabaseName(databaseProperties.name())
                .setClientProperty(AvailableSettings.DIALECT, H2Dialect.class.getName())
                .setClientProperty(AvailableSettings.SHOW_SQL, databaseProperties.showSql())
                .setClientProperty(AvailableSettings.FORMAT_SQL, databaseProperties.formatSql())
                .setClientProperty(AvailableSettings.HBM2DDL_AUTO, databaseProperties.hbm2ddlAuto());

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
                Student.class,
                Subject.class
        );
    }
}
