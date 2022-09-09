package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.jwt.JWTProvider;
import io.github.etases.edublock.rs.jwt.JavalinJWT;
import io.javalin.Javalin;

public class JwtHandler implements ServerHandler {
    @Inject
    private Javalin server;

    @Override
    public void setup() {
        Algorithm algorithm = Algorithm.HMAC256("very_secret");
        JWTVerifier verifier = JWT.require(algorithm).build();
        JWTProvider provider = new JWTProvider(algorithm, verifier);
        server.before(JavalinJWT.createHeaderDecodeHandler(provider));
    }
}
