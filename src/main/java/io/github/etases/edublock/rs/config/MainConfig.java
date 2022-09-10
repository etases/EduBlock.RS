package io.github.etases.edublock.rs.config;

import io.github.etases.edublock.rs.config.converter.JwtPropertiesConverter;
import io.github.etases.edublock.rs.handler.jwt.JwtProperties;
import me.hsgamer.hscore.config.annotation.ConfigPath;

public interface MainConfig {
    @ConfigPath(value = "jwt", converter = JwtPropertiesConverter.class)
    default JwtProperties getJwtProperties() {
        return new JwtProperties("very_secret", "edublock", "client", "edublock.rs");
    }
}
