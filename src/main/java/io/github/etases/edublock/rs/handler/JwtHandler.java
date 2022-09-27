package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.PasswordUtils;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.internal.jwt.JwtProvider;
import io.github.etases.edublock.rs.model.input.AccountLogin;
import io.github.etases.edublock.rs.model.output.Response;
import io.github.etases.edublock.rs.model.output.StringResponse;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.core.security.RouteRole;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import me.hsgamer.hscore.collections.map.CaseInsensitiveStringHashMap;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Map;
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

    @Override
    protected void setupConfig(JavalinConfig config) {
        config.accessManager(provider.createAccessManager(Roles.getRoleMapping(), Roles.ANYONE));
    }

    @Override
    protected void setupServer(Javalin server) {
        server.before(provider.createHeaderDecodeHandler());

        server.post("/login", new LoginHandler().handler());
        server.post("/register", new RegisterHandler().handler());
    }

    public enum Roles implements RouteRole {
        ANYONE, STUDENT, STAFF, TEACHER, ADMIN;

        public static boolean isValid(String role) {
            try {
                Roles.valueOf(role.toUpperCase());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

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

    private class LoginHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .body(AccountLogin.class)
                    .result("200", StringResponse.class, builder -> builder.description("The token"))
                    .result("401", StringResponse.class, builder -> builder.description("Invalid username or password"));
        }

        @Override
        public void handle(Context ctx) {
            AccountLogin accountLogin = ctx.bodyValidator(AccountLogin.class)
                    .check(input -> input.username() != null, "Username cannot be null")
                    .check(input -> input.password() != null, "Password cannot be null")
                    .get();
            ctx.future(
                    CompletableFuture.supplyAsync(() -> {
                        try (var session = sessionFactory.openSession()) {
                            return session.createNamedQuery("Account.findByUsername", Account.class)
                                    .setParameter("username", accountLogin.username())
                                    .uniqueResult();
                        }
                    }),
                    result -> {
                        Account account = result == null ? null : (Account) result;
                        if (account == null || !PasswordUtils.verifyPassword(accountLogin.password(), account.getSalt(), account.getHashedPassword())) {
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
                    }
            );
        }
    }

    private class RegisterHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .body(AccountLogin.class)
                    .result("200", Response.class, builder -> builder.description("The token"))
                    .result("409", Response.class, builder -> builder.description("Username already exists"));
        }

        @Override
        public void handle(Context ctx) {
            AccountLogin accountLogin = ctx.bodyValidator(AccountLogin.class)
                    .check(input -> input.username() != null, "Username cannot be null")
                    .check(input -> input.password() != null, "Password cannot be null")
                    .get();
            ctx.future(
                    CompletableFuture.supplyAsync(() -> {
                        try (var session = sessionFactory.openSession()) {
                            return session.createNamedQuery("Account.findByUsername", Account.class)
                                    .setParameter("username", accountLogin.username())
                                    .uniqueResult();
                        }
                    }),
                    result -> {
                        Account account = result == null ? null : (Account) result;
                        if (account != null) {
                            ctx.status(409);
                            ctx.json(new Response(1, "Username already exists"));
                            return;
                        }
                        String salt = PasswordUtils.generateSalt();
                        String hash = PasswordUtils.hashPassword(accountLogin.password(), salt);
                        account = new Account();
                        account.setUsername(accountLogin.username());
                        account.setHashedPassword(hash);
                        account.setSalt(salt);
                        account.setRole(Roles.ADMIN.name());
                        try (var session = sessionFactory.openSession()) {
                            Transaction transaction = session.beginTransaction();
                            session.save(account);
                            transaction.commit();
                        }
                        ctx.json(new Response(0, "Register Successful"));
                    }
            );
        }
    }
}
