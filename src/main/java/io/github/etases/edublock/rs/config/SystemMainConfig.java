package io.github.etases.edublock.rs.config;

import io.github.etases.edublock.rs.internal.property.DatabaseProperties;
import io.github.etases.edublock.rs.internal.property.JwtProperties;
import io.github.etases.edublock.rs.internal.property.ServerProperties;

import java.util.List;
import java.util.Optional;

public class SystemMainConfig implements MainConfig {
    private final JwtProperties jwtProperties;
    private final DatabaseProperties databaseProperties;
    private final ServerProperties serverProperties;
    private final String accountDefaultPassword;

    public SystemMainConfig() {
        this.jwtProperties = new JwtProperties(
                Optional.ofNullable(System.getenv("RS_JWT_SECRET")).orElse("very_secret"),
                Optional.ofNullable(System.getenv("RS_JWT_ISSUER")).orElse("edublock"),
                Optional.ofNullable(System.getenv("RS_JWT_AUDIENCE")).orElse("client"),
                Optional.ofNullable(System.getenv("RS_JWT_SUBJECT")).orElse("edublock.rs")
        );
        this.databaseProperties = new DatabaseProperties(
                Optional.ofNullable(System.getenv("RS_DATABASE_NAME")).orElse("edublock"),
                Optional.ofNullable(System.getenv("RS_DATABASE_USER")).orElse("root"),
                Optional.ofNullable(System.getenv("RS_DATABASE_PASSWORD")).orElse(""),
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_DATABASE_IS_MEMORY")).orElse("true"))
        );
        this.serverProperties = new ServerProperties(
                Optional.ofNullable(System.getenv("RS_SERVER_HOST")).orElse("localhost"),
                Integer.parseInt(Optional.ofNullable(System.getenv("RS_SERVER_PORT")).orElse("7070")),
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_SERVER_DEV_MODE")).orElse("true")),
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_SERVER_BYPASS_CORS")).orElse("true")),
                List.of(Optional.ofNullable(System.getenv("RS_SERVER_ALLOWED_ORIGINS")).orElse("*").split(","))
        );
        this.accountDefaultPassword = Optional.ofNullable(System.getenv("RS_ACCOUNT_DEFAULT_PASSWORD")).orElse("password");
    }

    @Override
    public JwtProperties getJwtProperties() {
        return jwtProperties;
    }

    @Override
    public DatabaseProperties getDatabaseProperties() {
        return databaseProperties;
    }

    @Override
    public ServerProperties getServerProperties() {
        return serverProperties;
    }

    @Override
    public String getDefaultPassword() {
        return accountDefaultPassword;
    }
}
