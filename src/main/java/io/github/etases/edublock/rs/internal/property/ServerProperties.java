package io.github.etases.edublock.rs.internal.property;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ServerProperties(String host, int port, boolean devLogging, boolean bypassCors,
                               List<String> allowedOrigins) {
    public static ServerProperties fromMap(Map<?, ?> map) {
        return new ServerProperties(
                Objects.toString(map.get("host"), "localhost"),
                Integer.parseInt(Objects.toString(map.get("port"), "7070")),
                Boolean.parseBoolean(Objects.toString(map.get("dev-logging"), "true")),
                Boolean.parseBoolean(Objects.toString(map.get("bypass-cors"), "true")),
                List.of(Objects.toString(map.get("allowed-origins"), "*").split(","))
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "host", host,
                "port", port,
                "dev-logging", devLogging,
                "bypass-cors", bypassCors,
                "allowed-origins", String.join(",", allowedOrigins)
        );
    }
}
