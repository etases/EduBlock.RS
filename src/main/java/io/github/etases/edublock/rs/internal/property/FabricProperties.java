package io.github.etases.edublock.rs.internal.property;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public record FabricProperties(boolean enabled, Path certPath, Path keyPath, String mspId, String address,
                               boolean tlsEnabled, Path tlsCertPath, String tlsOverrideAuthority) {
    public static FabricProperties fromMap(Map<?, ?> map) {
        return new FabricProperties(
                Boolean.parseBoolean(Objects.toString(map.get("enabled"), "false")),
                Path.of(Objects.toString(map.get("cert-path"), "cert.pem")),
                Path.of(Objects.toString(map.get("key-path"), "key.pem")),
                Objects.toString(map.get("msp-id"), "Org1MSP"),
                Objects.toString(map.get("address"), "ipv4:localhost:7051"),
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
                "address", address,
                "tls-enabled", tlsEnabled,
                "tls-cert-path", tlsCertPath.toString(),
                "tls-override-authority", tlsOverrideAuthority
        );
    }
}
