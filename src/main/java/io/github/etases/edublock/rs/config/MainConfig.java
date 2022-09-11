package io.github.etases.edublock.rs.config;

import io.github.etases.edublock.rs.config.converter.DatabasePropertiesConverter;
import io.github.etases.edublock.rs.config.converter.JwtPropertiesConverter;
import io.github.etases.edublock.rs.config.converter.ServerPropertiesConverter;
import io.github.etases.edublock.rs.internal.property.DatabaseProperties;
import io.github.etases.edublock.rs.internal.property.JwtProperties;
import io.github.etases.edublock.rs.internal.property.ServerProperties;
import me.hsgamer.hscore.config.annotation.ConfigPath;

public interface MainConfig {
    @ConfigPath(value = "jwt", converter = JwtPropertiesConverter.class)
    default JwtProperties getJwtProperties() {
        return new JwtProperties("very_secret", "edublock", "client", "edublock.rs");
    }

    @ConfigPath(value = "database", converter = DatabasePropertiesConverter.class)
    default DatabaseProperties getDatabaseProperties() {
        return new DatabaseProperties("edublock", true, true, true, "update");
    }

    @ConfigPath(value = "server", converter = ServerPropertiesConverter.class)
    default ServerProperties getServerProperties() {
        return new ServerProperties("localhost", 7070);
    }
}
