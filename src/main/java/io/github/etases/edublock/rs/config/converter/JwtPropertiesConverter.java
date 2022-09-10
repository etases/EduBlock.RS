package io.github.etases.edublock.rs.config.converter;

import io.github.etases.edublock.rs.handler.jwt.JwtProperties;
import me.hsgamer.hscore.config.annotation.converter.Converter;

import java.util.Map;

public class JwtPropertiesConverter implements Converter {
    @Override
    public Object convert(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            return JwtProperties.fromMap(map);
        }
        return null;
    }

    @Override
    public Object convertToRaw(Object value) {
        if (value instanceof JwtProperties properties) {
            return properties.toMap();
        }
        return null;
    }
}
