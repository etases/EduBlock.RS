package io.github.etases.edublock.rs.internal.property;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ServerProperties(String host, int port, boolean devMode, boolean bypassCors,
                               List<String> allowedOrigins) {
    public static ServerProperties fromMap(Map<?, ?> map) {
        return new ServerProperties(
                Objects.toString(map.get("host"), "0.0.0.0"),
                Integer.parseInt(Objects.toString(map.get("port"), "7070")),
                Boolean.parseBoolean(Objects.toString(map.get("dev-mode"), "true")),
                Boolean.parseBoolean(Objects.toString(map.get("bypass-cors"), "true")),
                List.of(Objects.toString(map.get("allowed-origins"), "*").split(","))
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "host", host,
                "port", port,
                "dev-mode", devMode,
                "bypass-cors", bypassCors,
                "allowed-origins", String.join(",", allowedOrigins)
        );
    }
}
