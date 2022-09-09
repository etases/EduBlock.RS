package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.jwt.JwtProvider;
import io.github.etases.edublock.rs.jwt.JwtUtil;
import io.javalin.Javalin;

public class JwtHandler implements ServerHandler {
    @Inject
    private Javalin server;

    @Override
    public void setup() {
        Algorithm algorithm = Algorithm.HMAC256("very_secret");
        JWTVerifier verifier = JWT.require(algorithm).build();
        JwtProvider provider = new JwtProvider(algorithm, verifier);
        server.before(JwtUtil.createHeaderDecodeHandler(provider));
    }
}
