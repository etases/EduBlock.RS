package io.github.etases.edublock.rs;

import io.javalin.core.security.RouteRole;
import me.hsgamer.hscore.collections.map.CaseInsensitiveStringHashMap;

import java.util.Map;

public enum Roles implements RouteRole {
    ANYONE,
    USER,
    ADMIN;

    public static Map<String, RouteRole> getRoleMapping() {
        Map<String, RouteRole> roleMap = new CaseInsensitiveStringHashMap<>();
        for (Roles role : Roles.values()) {
            roleMap.put(role.name(), role);
        }
        return roleMap;
    }
}
