package io.github.etases.edublock.rs.config;

import io.github.etases.edublock.rs.config.converter.*;
import io.github.etases.edublock.rs.internal.property.*;
import me.hsgamer.hscore.config.annotation.ConfigPath;

import java.util.Collections;

public interface MainConfig {
    @ConfigPath(value = "jwt", converter = JwtPropertiesConverter.class)
    default JwtProperties getJwtProperties() {
        return JwtProperties.fromMap(Collections.emptyMap());
    }

    @ConfigPath(value = "database", converter = DatabasePropertiesConverter.class)
    default DatabaseProperties getDatabaseProperties() {
        return DatabaseProperties.fromMap(Collections.emptyMap());
    }

    @ConfigPath(value = "server", converter = ServerPropertiesConverter.class)
    default ServerProperties getServerProperties() {
        return ServerProperties.fromMap(Collections.emptyMap());
    }

    @ConfigPath(value = "fabric-peer", converter = FabricPropertiesConverter.class)
    default FabricProperties getFabricProperties() {
        return FabricProperties.fromMap(Collections.emptyMap());
    }

    @ConfigPath(value = "fabric-updater", converter = FabricUpdaterPropertiesConverter.class)
    default FabricUpdaterProperties getFabricUpdaterProperties() {
        return FabricUpdaterProperties.fromMap(Collections.emptyMap());
    }

    @ConfigPath(value = "account.default-password")
    default String getDefaultPassword() {
        return "password";
    }

    @ConfigPath(value = "updater.period")
    default int getUpdaterPeriod() {
        return 60;
    }

    @ConfigPath(value = "student.one-class-per-year")
    default boolean isOneClassPerYear() {
        return true;
    }
}
