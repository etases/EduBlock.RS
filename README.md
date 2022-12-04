# EduBlock.RS

The Request Server for EduBlock

## Deployment guide

### Build image

```sh
docker build --tag edublock-rs:local .
```

### Deploy container

```sh
docker run --name edublock-rs --interactive --rm --publish 7070:7070 edublock-rs:local
docker run --name edublock-rs --interactive --rm --volume $PWD/docker-data:/data --publish 7070:7070 edublock-rs:local
```

## System Environment Variables

| Key                                   | Type | Description                               | Default                |
|---------------------------------------|------|-------------------------------------------|------------------------|
| RS_CONFIG_USE_SYSTEM                  | bool | Use system environment config             | false                  |
| RS_JWT_SECRET                         | str  | JWT secret                                | very_secret            |
| RS_JWT_ISSUER                         | str  | JWT issuer                                | edublock               |
| RS_JWT_AUDIENCE                       | str  | JWT audience                              | client                 |
| RS_JWT_SUBJECT                        | str  | JWT subject                               | edublock.rs            |
| RS_DATABASE_NAME                      | str  | Database name                             | edublock               |
| RS_DATABASE_USER                      | str  | Database user                             | root                   |
| RS_DATABASE_PASSWORD                  | str  | Database password                         |                        |
| RS_DATABASE_HOST                      | str  | Database host                             | 0.0.0.0              |
| RS_DATABASE_PORT                      | int  | Database port                             | 3306                   |
| RS_DATABASE_IS_SSH_TUNNEL             | bool | Use SSH tunnel to connect to database     | false                  |
| RS_DATABASE_IS_FILE                   | bool | Use file to store database                | true                   |
| RS_DATABASE_IS_MEMORY                 | bool | Use memory database                       | true                   |
| RS_SERVER_HOST                        | str  | Server host                               | 0.0.0.0              |
| RS_SERVER_PORT                        | int  | Server port                               | 7070                   |
| RS_SERVER_DEV_MODE                    | bool | Server development mode                   | true                   |
| RS_SERVER_BYPASS_CORS                 | bool | Server bypass CORS                        | true                   |
| RS_SERVER_ALLOWED_ORIGINS             | str  | Server allowed origins                    | *                      |
| RS_FABRIC_PEER_ENABLED                | bool | Enable fabric peer                        | false                  |
| RS_FABRIC_PEER_CERT_PEM               | str  | Fabric peer cert pem                      |                        |
| RS_FABRIC_PEER_KEY_PEM                | str  | Fabric peer key pem                       |                        |
| RS_FABRIC_PEER_MSP_ID                 | str  | Fabric peer msp id                        | Org1MSP                |
| RS_FABRIC_PEER_INET_ADDRESS           | bool | Fabric peer host is inet address          | true                   |
| RS_FABRIC_PEER_HOST                   | str  | Fabric peer host                          | 0.0.0.0              |
| RS_FABRIC_PEER_PORT                   | int  | Fabric peer port                          | 7051                   |
| RS_FABRIC_PEER_TLS_ENABLED            | bool | Fabric peer tls enabled                   | false                  |
| RS_FABRIC_PEER_TLS_CERT_PEM           | str  | Fabric peer tls cert pem                  |                        |
| RS_FABRIC_PEER_TLS_OVERRIDE_AUTHORITY | bool | Fabric peer tls override authority        | peer0.org1.example.com |
| RS_FABRIC_UPDATER_CHANNEL_NAME        | str  | Fabric student updater channel name       | mychannel              |
| RS_FABRIC_UPDATER_CHAINCODE_NAME      | str  | Fabric student updater chaincode name     | edublock               |
| RS_ACCOUNT_DEFAULT_PASSWORD           | str  | The default password of new accounts      | password               |
| RS_UPDATER_PERIOD                     | int  | The period of student updater             | 1000                   |
| RS_ONE_CLASS_PER_YEAR                 | bool | Limit students to join one class per year | true                   |