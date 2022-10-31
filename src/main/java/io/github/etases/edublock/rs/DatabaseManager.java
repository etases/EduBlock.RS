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
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

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
                .setUsername(databaseProperties.username())
                .setPassword(databaseProperties.password())
                .setClientProperty(AvailableSettings.DIALECT, H2Dialect.class.getName());

        if (requestServer.getMainConfig().getServerProperties().devMode()) {
            setting
                    .setClientProperty(AvailableSettings.SHOW_SQL, true)
                    .setClientProperty(AvailableSettings.FORMAT_SQL, true)
                    .setClientProperty(AvailableSettings.HBM2DDL_AUTO, "drop-and-create");
            File dataSqlFile = new File("data.sql");
            if (!dataSqlFile.exists()) {
                try {
                    if (dataSqlFile.createNewFile()) {
                        Files.copy(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("data.sql")), dataSqlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            setting.setClientProperty(AvailableSettings.HBM2DDL_LOAD_SCRIPT_SOURCE, dataSqlFile.getAbsolutePath());
        } else {
            setting.setClientProperty(AvailableSettings.HBM2DDL_AUTO, "update");
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
                Student.class,
                Subject.class
        );
    }
}
