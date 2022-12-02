package io.github.etases.edublock.rs.internal.property;

import java.util.Map;
import java.util.Objects;

public record DatabaseProperties(String name, String username, String password, String host, String port, boolean isSSH,
                                 boolean isFile, boolean isMemory) {
    public static DatabaseProperties fromMap(Map<?, ?> map) {
        return new DatabaseProperties(
                Objects.toString(map.get("name"), "edublock"),
                Objects.toString(map.get("username"), "root"),
                Objects.toString(map.get("password"), ""),
                Objects.toString(map.get("host"), "localhost"),
                Objects.toString(map.get("port"), "3306"),
                Boolean.parseBoolean(Objects.toString(map.get("is-ssh"), "false")),
                Boolean.parseBoolean(Objects.toString(map.get("is-file"), "true")),
                Boolean.parseBoolean(Objects.toString(map.get("is-memory"), "true"))
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "name", name,
                "username", username,
                "password", password,
                "host", host,
                "port", port,
                "is-ssh", isSSH,
                "is-file", isFile,
                "is-memory", isMemory
        );
    }
}
