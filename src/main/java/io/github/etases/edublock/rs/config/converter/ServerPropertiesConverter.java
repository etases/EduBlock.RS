package io.github.etases.edublock.rs.config.converter;

import io.github.etases.edublock.rs.internal.property.ServerProperties;
import me.hsgamer.hscore.config.annotation.converter.Converter;

import java.util.Map;

public class ServerPropertiesConverter implements Converter {
    @Override
    public Object convert(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            return ServerProperties.fromMap(map);
        }
        return null;
    }

    @Override
    public Object convertToRaw(Object value) {
        if (value instanceof ServerProperties properties) {
            return properties.toMap();
        }
        return null;
    }
}
