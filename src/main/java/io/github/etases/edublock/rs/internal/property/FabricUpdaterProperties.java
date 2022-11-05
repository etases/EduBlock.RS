package io.github.etases.edublock.rs.internal.property;

import java.util.Map;
import java.util.Objects;

public record FabricUpdaterProperties(String channelName, String chaincodeName) {
    public static FabricUpdaterProperties fromMap(Map<?, ?> map) {
        return new FabricUpdaterProperties(
                Objects.toString(map.get("channel-name"), "mychannel"),
                Objects.toString(map.get("chaincode-name"), "edublock")
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "channel-name", channelName,
                "chaincode-name", chaincodeName
        );
    }
}
