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
```
