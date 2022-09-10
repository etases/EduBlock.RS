package io.github.etases.edublock.rs.handler.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public final class JwtUtil {

    private static final String CONTEXT_ATTRIBUTE = "jwt";


    public static boolean containsJwt(Context context) {
        return context.attribute(CONTEXT_ATTRIBUTE) != null;
    }

    public static Context addDecodedToContext(Context context, DecodedJWT jwt) {
        context.attribute(CONTEXT_ATTRIBUTE, jwt);
        return context;
    }

    public static DecodedJWT getDecodedFromContext(Context context) {
        Object attribute = context.attribute(CONTEXT_ATTRIBUTE);

        if (!(attribute instanceof DecodedJWT)) {
            throw new InternalServerErrorResponse("The context carried invalid object as JwtUtil");
        }

        return (DecodedJWT) attribute;
    }

    public static Optional<String> getTokenFromHeader(Context context) {
        return Optional.ofNullable(context.header("Authorization"))
                .flatMap(header -> {
                    String[] split = header.split(" ");
                    if (split.length != 2 || !split[0].equals("Bearer")) {
                        return Optional.empty();
                    }

                    return Optional.of(split[1]);
                });
    }
}
