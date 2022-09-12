package io.github.etases.edublock.rs.internal.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.etases.edublock.rs.internal.property.JwtProperties;
import io.github.etases.edublock.rs.model.output.Response;
import io.javalin.core.security.AccessManager;
import io.javalin.core.security.RouteRole;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JwtProvider {
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final String userRoleClaim;
    @Getter
    private final JwtProperties properties;

    public JwtProvider(String userRoleClaim, JwtProperties properties) {
        this.userRoleClaim = userRoleClaim;
        this.properties = properties;
        this.algorithm = Algorithm.HMAC256(properties.secret());
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(JWTCreator.Builder builder) {
        return builder
                .withAudience(properties.audience())
                .withIssuer(properties.issuer())
                .withSubject(properties.subject())
                .sign(algorithm);
    }

    public Optional<DecodedJWT> validateToken(String token) {
        try {
            return Optional.of(verifier.verify(token));
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }
    }

    public AccessManager createAccessManager(Map<String, RouteRole> roleMapping, RouteRole defaultRole) {
        return new AccessManager() {
            private Optional<DecodedJWT> getDecodedJwt(Context context) {
                if (JwtUtil.containsJwt(context)) {
                    return Optional.of(JwtUtil.getDecodedFromContext(context));
                } else {
                    return Optional.empty();
                }
            }

            private RouteRole getRoleFromJwt(DecodedJWT jwt) {
                return roleMapping.get(jwt.getClaim(userRoleClaim).asString());
            }

            @Override
            public void manage(@NotNull Handler handler, @NotNull Context context, @NotNull Set<RouteRole> permittedRoles) throws Exception {
                Optional<DecodedJWT> optionalJwt = getDecodedJwt(context);
                boolean matchProperties = optionalJwt.map(properties::isMatch).orElse(false);
                RouteRole role = optionalJwt.map(this::getRoleFromJwt).orElse(defaultRole);

                if (permittedRoles.isEmpty() || (matchProperties && permittedRoles.contains(role))) {
                    handler.handle(context);
                } else {
                    context.status(401).json(new Response(-1990, "Unauthorized"));
                }
            }
        };
    }

    public Handler createHeaderDecodeHandler() {
        return context -> JwtUtil.getTokenFromHeader(context)
                .flatMap(this::validateToken)
                .ifPresent(jwt -> JwtUtil.addDecodedToContext(context, jwt));
    }
}
