package io.github.etases.edublock.rs.internal.property;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public record FabricProperties(boolean enabled, Path certPath, Path keyPath, String mspId, boolean inetAddress, String host, int port,
                               boolean tlsEnabled, Path tlsCertPath, String tlsOverrideAuthority) {
    public static FabricProperties fromMap(Map<?, ?> map) {
        return new FabricProperties(
                Boolean.parseBoolean(Objects.toString(map.get("enabled"), "false")),
                Path.of(Objects.toString(map.get("cert-path"), "cert.pem")),
                Path.of(Objects.toString(map.get("key-path"), "key.pem")),
                Objects.toString(map.get("msp-id"), "Org1MSP"),
                Boolean.parseBoolean(Objects.toString(map.get("inet-address"), "true")),
                Objects.toString(map.get("host"), "localhost"),
                Integer.parseInt(Objects.toString(map.get("port"), "7051")),
                Boolean.parseBoolean(Objects.toString(map.get("tls-enabled"), "false")),
                Path.of(Objects.toString(map.get("tls-cert-path"), "tls-cert.pem")),
                Objects.toString(map.get("tls-override-authority"), "peer0.org1.example.com")
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "enabled", enabled,
                "cert-path", certPath.toString(),
                "key-path", keyPath.toString(),
                "msp-id", mspId,
                "inet-address", inetAddress,
                "host", host,
                "port", port,
                "tls-enabled", tlsEnabled,
                "tls-cert-path", tlsCertPath.toString(),
                "tls-override-authority", tlsOverrideAuthority
        );
    }
}
