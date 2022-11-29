package io.github.etases.edublock.rs.internal.property;

import me.hsgamer.hscore.common.CollectionUtils;

import java.util.Map;
import java.util.Objects;

public record FabricProperties(boolean enabled, String certPem, String keyPem, String mspId, boolean inetAddress,
                               String host, int port,
                               boolean tlsEnabled, String tlsCertPem, String tlsOverrideAuthority) {
    public static FabricProperties fromMap(Map<?, ?> map) {
        return new FabricProperties(
                Boolean.parseBoolean(Objects.toString(map.get("enabled"), "false")),
                getPem(map.get("cert-pem")),
                getPem(map.get("key-pem")),
                Objects.toString(map.get("msp-id"), "Org1MSP"),
                Boolean.parseBoolean(Objects.toString(map.get("inet-address"), "true")),
                Objects.toString(map.get("host"), "localhost"),
                Integer.parseInt(Objects.toString(map.get("port"), "7051")),
                Boolean.parseBoolean(Objects.toString(map.get("tls-enabled"), "false")),
                getPem(map.get("tls-cert-pem")),
                Objects.toString(map.get("tls-override-authority"), "peer0.org1.example.com")
        );
    }

    private static String getPem(Object object) {
        if (object == null) {
            return "";
        }
        return String.join("\n", CollectionUtils.createStringListFromObject(object));
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "enabled", enabled,
                "cert-pem", certPem,
                "key-pem", keyPem,
                "msp-id", mspId,
                "inet-address", inetAddress,
                "host", host,
                "port", port,
                "tls-enabled", tlsEnabled,
                "tls-cert-pem", tlsCertPem,
                "tls-override-authority", tlsOverrideAuthority
        );
    }
}
