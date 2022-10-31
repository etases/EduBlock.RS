package io.github.etases.edublock.rs.internal.property;

import java.util.Map;
import java.util.Objects;

public record DatabaseProperties(String name, String username, String password, boolean isMemory) {
    public static DatabaseProperties fromMap(Map<?, ?> map) {
        return new DatabaseProperties(
                Objects.toString(map.get("name"), "edublock"),
                Objects.toString(map.get("username"), "root"),
                Objects.toString(map.get("password"), ""),
                Boolean.parseBoolean(Objects.toString(map.get("is-memory"), "true"))
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "name", name,
                "username", username,
                "password", password,
                "is-memory", isMemory
        );
    }
}
