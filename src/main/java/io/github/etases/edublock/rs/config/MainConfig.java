package io.github.etases.edublock.rs.config;

import io.github.etases.edublock.rs.config.converter.DatabasePropertiesConverter;
import io.github.etases.edublock.rs.config.converter.FabricPropertiesConverter;
import io.github.etases.edublock.rs.config.converter.JwtPropertiesConverter;
import io.github.etases.edublock.rs.config.converter.ServerPropertiesConverter;
import io.github.etases.edublock.rs.internal.property.DatabaseProperties;
import io.github.etases.edublock.rs.internal.property.FabricProperties;
import io.github.etases.edublock.rs.internal.property.JwtProperties;
import io.github.etases.edublock.rs.internal.property.ServerProperties;
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

    @ConfigPath(value = "account.default-password")
    default String getDefaultPassword() {
        return "password";
    }
}
