package io.github.etases.edublock.rs.config.converter;

import io.github.etases.edublock.rs.internal.property.FabricProperties;
import me.hsgamer.hscore.config.annotation.converter.Converter;

import java.util.Map;

public class FabricPropertiesConverter implements Converter {
    @Override
    public Object convert(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            return FabricProperties.fromMap(map);
        }
        return null;
    }

    @Override
    public Object convertToRaw(Object value) {
        if (value instanceof FabricProperties properties) {
            return properties.toMap();
        }
        return null;
    }
}
