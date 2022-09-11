package io.github.etases.edublock.rs.internal.property;

import java.util.Map;
import java.util.Objects;

public record DatabaseProperties(String name, boolean isMemory, boolean showSql, boolean formatSql,
                                 String hbm2ddlAuto) {
    public static DatabaseProperties fromMap(Map<?, ?> map) {
        return new DatabaseProperties(
                Objects.toString(map.get("name"), ""),
                Boolean.parseBoolean(Objects.toString(map.get("is-memory"), "true")),
                Boolean.parseBoolean(Objects.toString(map.get("show-sql"), "true")),
                Boolean.parseBoolean(Objects.toString(map.get("format-sql"), "true")),
                Objects.toString(map.get("hbm2ddlAuto"), "update")
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "name", name,
                "is-memory", isMemory,
                "show-sql", showSql,
                "format-sql", formatSql,
                "hbm2ddlAuto", hbm2ddlAuto
        );
    }
}
