package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.entity.User;
import io.github.etases.edublock.rs.internal.jwt.JwtProvider;
import io.github.etases.edublock.rs.model.input.UserInput;
import io.github.etases.edublock.rs.model.output.LoginResponse;
import io.github.etases.edublock.rs.model.output.Response;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.core.security.RouteRole;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import me.hsgamer.hscore.collections.map.CaseInsensitiveStringHashMap;
import org.apache.commons.text.RandomStringGenerator;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class JwtHandler extends SimpleServerHandler {
    private static final String USER_ROLE_CLAIM = "role";
    private static final RandomStringGenerator saltGenerator;
    private static final SecretKeyFactory keyFactory;

    static {
        saltGenerator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(Character::isLetterOrDigit)
                .build();
        try {
            keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private final JwtProvider provider;
    private final SessionFactory sessionFactory;

    @Inject
    public JwtHandler(ServerBuilder serverBuilder, MainConfig mainConfig, SessionFactory sessionFactory) {
        super(serverBuilder);
        provider = new JwtProvider(USER_ROLE_CLAIM, mainConfig.getJwtProperties());
        this.sessionFactory = sessionFactory;
    }

    private static String generateSalt() {
        return saltGenerator.generate(16, 32);
    }

    private static String hashPassword(String password, String salt) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 128);
        try {
            byte[] hash = keyFactory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean verifyPassword(String password, String salt, String hash) {
        return hashPassword(password, salt).equals(hash);
    }

    @Override
    protected void setupConfig(JavalinConfig config) {
        config.accessManager(provider.createAccessManager(Roles.getRoleMapping(), Roles.ANYONE));
    }

    @Override
    protected void setupServer(Javalin server) {
        server.before(provider.createHeaderDecodeHandler());

        server.post("/login", OpenApiBuilder.documented(
                OpenApiBuilder.document()
                        .body(UserInput.class)
                        .result("200", LoginResponse.class, builder -> builder.description("The token"))
                        .result("401", LoginResponse.class, builder -> builder.description("Invalid username or password")),
                ctx -> {
                    UserInput userInput = ctx.bodyAsClass(UserInput.class);
                    ctx.future(
                            CompletableFuture.supplyAsync(() -> {
                                try (var session = sessionFactory.openSession()) {
                                    return session.createNamedQuery("User.findByUsername", User.class)
                                            .setParameter("username", userInput.username())
                                            .uniqueResult();
                                }
                            }),
                            result -> {
                                User user = result == null ? null : (User) result;
                                if (user == null || !verifyPassword(userInput.password(), user.getSalt(), user.getHashedPassword())) {
                                    ctx.status(401);
                                    ctx.json(new LoginResponse(1, "Invalid username or password", null));
                                    return;
                                }
                                JWTCreator.Builder builder = JWT.create()
                                        .withClaim(USER_ROLE_CLAIM, user.getRole())
                                        .withClaim("name", user.getUsername())
                                        .withClaim("id", user.getId());
                                String token = provider.generateToken(builder);
                                ctx.json(new LoginResponse(0, "Login Successful", token));
                            }
                    );
                }
        ));

        server.post("/register", OpenApiBuilder.documented(
                OpenApiBuilder.document()
                        .body(UserInput.class)
                        .result("200", Response.class, builder -> builder.description("The token"))
                        .result("409", Response.class, builder -> builder.description("Username already exists")),
                ctx -> {
                    UserInput userInput = ctx.bodyAsClass(UserInput.class);
                    ctx.future(
                            CompletableFuture.supplyAsync(() -> {
                                try (var session = sessionFactory.openSession()) {
                                    return session.createNamedQuery("User.findByUsername", User.class)
                                            .setParameter("username", userInput.username())
                                            .uniqueResult();
                                }
                            }),
                            result -> {
                                User user = result == null ? null : (User) result;
                                if (user != null) {
                                    ctx.status(409);
                                    ctx.json(new Response(1, "Username already exists"));
                                    return;
                                }
                                String salt = generateSalt();
                                String hash = hashPassword(userInput.password(), salt);
                                user = new User();
                                user.setUsername(userInput.username());
                                user.setHashedPassword(hash);
                                user.setSalt(salt);
                                user.setRole(Roles.USER.name());
                                try (var session = sessionFactory.openSession()) {
                                    Transaction transaction = session.beginTransaction();
                                    session.save(user);
                                    transaction.commit();
                                }
                                ctx.json(new Response(0, "Register Successful"));
                            }
                    );
                }
        ));
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
