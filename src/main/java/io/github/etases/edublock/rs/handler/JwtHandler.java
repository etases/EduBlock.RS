package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.JWTCreator;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.internal.jwt.JwtProvider;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.core.security.RouteRole;
import me.hsgamer.hscore.collections.map.CaseInsensitiveStringHashMap;

import java.util.Map;

public class JwtHandler extends SimpleServerHandler {
    private static final String USER_ROLE_CLAIM = "role";
    private final JwtProvider provider;

    @Inject
    public JwtHandler(ServerBuilder serverBuilder, MainConfig mainConfig) {
        super(serverBuilder);
        provider = new JwtProvider(USER_ROLE_CLAIM, mainConfig.getJwtProperties());
    }

    @Override
    protected void setupConfig(JavalinConfig config) {
        config.accessManager(provider.createAccessManager(Roles.getRoleMapping(), Roles.ANYONE));
    }

    @Override
    protected void setupServer(Javalin server) {
        server.before(provider.createHeaderDecodeHandler());
    }

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

        public JWTCreator.Builder addRoleToToken(JWTCreator.Builder builder) {
            return builder.withClaim(USER_ROLE_CLAIM, name());
        }
    }
}
