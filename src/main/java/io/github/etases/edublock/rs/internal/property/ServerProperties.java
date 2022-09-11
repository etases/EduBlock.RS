package io.github.etases.edublock.rs.internal.property;

import java.util.Map;
import java.util.Objects;

public record ServerProperties(String host, int port) {
    public static ServerProperties fromMap(Map<?, ?> map) {
        return new ServerProperties(
                Objects.toString(map.get("host"), "localhost"),
                Integer.parseInt(Objects.toString(map.get("port"), "7070"))
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "host", host,
                "port", port
        );
    }
}
