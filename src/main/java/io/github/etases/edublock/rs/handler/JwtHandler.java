package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.PasswordUtils;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.internal.jwt.JwtProvider;
import io.github.etases.edublock.rs.internal.jwt.JwtUtil;
import io.github.etases.edublock.rs.model.input.AccountLogin;
import io.github.etases.edublock.rs.model.output.StringResponse;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import io.javalin.security.RouteRole;
import me.hsgamer.hscore.collections.map.CaseInsensitiveStringHashMap;
import org.hibernate.SessionFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JwtHandler extends SimpleServerHandler {
    private static final String USER_ROLE_CLAIM = "role";

    private final JwtProvider provider;
    private final SessionFactory sessionFactory;

    @Inject
    public JwtHandler(ServerBuilder serverBuilder, MainConfig mainConfig, SessionFactory sessionFactory) {
        super(serverBuilder);
        provider = new JwtProvider(USER_ROLE_CLAIM, mainConfig.getJwtProperties());
        this.sessionFactory = sessionFactory;
    }

    public static long getUserId(Context ctx) {
        DecodedJWT jwt = JwtUtil.getDecodedFromContext(ctx);
        return jwt.getClaim("id").asLong();
    }

    @Override
    protected void setupConfig(JavalinConfig config) {
        config.accessManager(provider.createAccessManager(Role.getRoleMapping(), Role.ANYONE));
    }

    @Override
    protected void setupServer(Javalin server) {
        server.before(provider.createHeaderDecodeHandler());
        server.post("/login", this::login);
    }

    @OpenApi(
            path = "/login",
            methods = HttpMethod.POST,
            summary = "Login",
            description = "Login to get the token",
            tags = {"Authentication"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AccountLogin.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = StringResponse.class),
                            description = "The token"
                    ),
                    @OpenApiResponse(
                            status = "401",
                            content = @OpenApiContent(from = StringResponse.class),
                            description = "Invalid username or password"
                    )
            }
    )
    private void login(Context ctx) {
        AccountLogin accountLogin = ctx.bodyValidator(AccountLogin.class)
                .check(input -> input.getUsername() != null, "Username cannot be null")
                .check(input -> input.getPassword() != null, "Password cannot be null")
                .get();
        ctx.future(
                () -> CompletableFuture.supplyAsync(() -> {
                    try (var session = sessionFactory.openSession()) {
                        return session.createNamedQuery("Account.findByUsername", Account.class)
                                .setParameter("username", accountLogin.getUsername())
                                .uniqueResult();
                    }
                }).thenAccept(account -> {
                    if (account == null || !PasswordUtils.verifyPassword(accountLogin.getPassword(), account.getSalt(), account.getHashedPassword())) {
                        ctx.status(401);
                        ctx.json(new StringResponse(1, "Invalid username or password", null));
                        return;
                    }
                    JWTCreator.Builder builder = JWT.create()
                            .withClaim(USER_ROLE_CLAIM, account.getRole())
                            .withClaim("name", account.getUsername())
                            .withClaim("id", account.getId());
                    String token = provider.generateToken(builder);
                    ctx.json(new StringResponse(0, "Login Successful", token));
                })
        );
    }

    public enum Role implements RouteRole {
        ANYONE, STUDENT, STAFF, TEACHER, ADMIN;

        public static Role[] authenticated() {
            return new Role[]{STUDENT, STAFF, TEACHER, ADMIN};
        }

        public static Role getRole(String role) {
            try {
                return Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ANYONE;
            }
        }

        public static Optional<Role> getRoleOptional(String role) {
            try {
                return Optional.of(Role.valueOf(role.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        public static Map<String, RouteRole> getRoleMapping() {
            Map<String, RouteRole> roleMap = new CaseInsensitiveStringHashMap<>();
            for (Role role : Role.values()) {
                roleMap.put(role.name(), role);
            }
            return roleMap;
        }

        public JWTCreator.Builder addRoleToToken(JWTCreator.Builder builder) {
            return builder.withClaim(USER_ROLE_CLAIM, name());
        }
    }
}
