package io.github.etases.edublock.rs.config.converter;

import io.github.etases.edublock.rs.internal.property.DatabaseProperties;
import me.hsgamer.hscore.config.annotation.converter.Converter;

import java.util.Map;

public class DatabasePropertiesConverter implements Converter {
    @Override
    public Object convert(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            return DatabaseProperties.fromMap(map);
        }
        return null;
    }

    @Override
    public Object convertToRaw(Object value) {
        if (value instanceof DatabaseProperties properties) {
            return properties.toMap();
        }
        return null;
    }
}
