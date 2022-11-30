package io.github.etases.edublock.rs.config;

import io.github.etases.edublock.rs.internal.property.*;

import java.util.List;
import java.util.Optional;

public class SystemMainConfig implements MainConfig {
    private final JwtProperties jwtProperties;
    private final DatabaseProperties databaseProperties;
    private final ServerProperties serverProperties;
    private final FabricProperties fabricProperties;
    private final FabricUpdaterProperties fabricUpdaterProperties;
    private final String accountDefaultPassword;
    private final int updaterPeriod;

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
                Optional.ofNullable(System.getenv("RS_DATABASE_HOST")).orElse("localhost"),
                Optional.ofNullable(System.getenv("RS_DATABASE_PORT")).orElse("3306"),
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_DATABASE_IS_SSH")).orElse("false")),
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_DATABASE_IS_FILE")).orElse("false")),
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_DATABASE_IS_MEMORY")).orElse("true"))
        );
        this.serverProperties = new ServerProperties(
                Optional.ofNullable(System.getenv("RS_SERVER_HOST")).orElse("localhost"),
                Integer.parseInt(Optional.ofNullable(System.getenv("RS_SERVER_PORT")).orElse("7070")),
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_SERVER_DEV_MODE")).orElse("true")),
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_SERVER_BYPASS_CORS")).orElse("true")),
                List.of(Optional.ofNullable(System.getenv("RS_SERVER_ALLOWED_ORIGINS")).orElse("*").split(","))
        );
        this.fabricProperties = new FabricProperties(
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_FABRIC_PEER_ENABLED")).orElse("false")),
                Optional.ofNullable(System.getenv("RS_FABRIC_PEER_CERT_PEM")).orElse(""),
                Optional.ofNullable(System.getenv("RS_FABRIC_PEER_KEY_PEM")).orElse(""),
                Optional.ofNullable(System.getenv("RS_FABRIC_PEER_MSP_ID")).orElse("Org1MSP"),
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_FABRIC_PEER_INET_ADDRESS")).orElse("true")),
                Optional.ofNullable(System.getenv("RS_FABRIC_PEER_HOST")).orElse("localhost"),
                Integer.parseInt(Optional.ofNullable(System.getenv("RS_FABRIC_PEER_PORT")).orElse("7051")),
                Boolean.parseBoolean(Optional.ofNullable(System.getenv("RS_FABRIC_PEER_TLS_ENABLED")).orElse("true")),
                Optional.ofNullable(System.getenv("RS_FABRIC_PEER_TLS_CERT_PEM")).orElse(""),
                Optional.ofNullable(System.getenv("RS_FABRIC_PEER_TLS_OVERRIDE_AUTHORITY")).orElse("peer0.org1.example.com")
        );
        this.fabricUpdaterProperties = new FabricUpdaterProperties(
                Optional.ofNullable(System.getenv("RS_FABRIC_UPDATER_CHANNEL_NAME")).orElse("mychannel"),
                Optional.ofNullable(System.getenv("RS_FABRIC_UPDATER_CHAINCODE_NAME")).orElse("edublock")
        );
        this.accountDefaultPassword = Optional.ofNullable(System.getenv("RS_ACCOUNT_DEFAULT_PASSWORD")).orElse("password");
        this.updaterPeriod = Integer.parseInt(Optional.ofNullable(System.getenv("RS_UPDATER_PERIOD")).orElse("60"));
    }

    public static boolean isSystemConfigEnabled() {
        String env = System.getenv("RS_CONFIG_USE_SYSTEM");
        return env != null && env.equalsIgnoreCase("true");
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
    public FabricProperties getFabricProperties() {
        return fabricProperties;
    }

    @Override
    public FabricUpdaterProperties getFabricUpdaterProperties() {
        return fabricUpdaterProperties;
    }

    @Override
    public String getDefaultPassword() {
        return accountDefaultPassword;
    }

    @Override
    public int getUpdaterPeriod() {
        return updaterPeriod;
    }
}
