package io.github.etases.edublock.rs.handler.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Map;
import java.util.Objects;

public record JwtProperties(String secret, String issuer, String audience, String subject) {
    public static JwtProperties fromMap(Map<?, ?> map) {
        return new JwtProperties(
                Objects.toString(map.get("secret"), ""),
                Objects.toString(map.get("issuer"), ""),
                Objects.toString(map.get("audience"), ""),
                Objects.toString(map.get("subject"), "")
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "secret", secret,
                "issuer", issuer,
                "audience", audience,
                "subject", subject
        );
    }

    public boolean isMatch(DecodedJWT jwt) {
        return jwt.getIssuer().equals(issuer)
                && jwt.getSubject().equals(subject)
                && jwt.getAudience().contains(audience);
    }
}
