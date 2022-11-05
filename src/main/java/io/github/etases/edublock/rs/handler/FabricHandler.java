package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.CommandManager;
import io.github.etases.edublock.rs.api.Command;
import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.command.fabric.InvokeEvaluateCommand;
import io.github.etases.edublock.rs.command.fabric.InvokeSubmitCommand;
import io.github.etases.edublock.rs.config.MainConfig;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.Getter;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.tinylog.Logger;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FabricHandler implements ServerHandler {
    @Inject
    private MainConfig config;
    @Inject
    private CommandManager commandManager;

    private ManagedChannel grpcChannel;
    @Getter
    private Gateway gateway;

    @Override
    public void setup() {
        var fabricProperties = config.getFabricProperties();
        if (!fabricProperties.enabled()) {
            Logger.info("Fabric is disabled");
            return;
        }

        try {
            var certReader = Files.newBufferedReader(fabricProperties.certPath());
            var certificate = Identities.readX509Certificate(certReader);
            var identity = new X509Identity(fabricProperties.mspId(), certificate);

            var keyPath = fabricProperties.keyPath();
            var keyReader = Files.newBufferedReader(keyPath);
            var privateKey = Identities.readPrivateKey(keyReader);
            var signer = Signers.newPrivateKeySigner(privateKey);

            var grpcChannelBuilder = fabricProperties.inetAddress()
                    ? NettyChannelBuilder.forAddress(new InetSocketAddress(fabricProperties.host(), fabricProperties.port()))
                    : NettyChannelBuilder.forTarget(fabricProperties.host());
            if (fabricProperties.tlsEnabled()) {
                var tlsCertReader = Files.newBufferedReader(fabricProperties.tlsCertPath());
                var tlsCert = Identities.readX509Certificate(tlsCertReader);
                grpcChannelBuilder = grpcChannelBuilder.sslContext(GrpcSslContexts.forClient().trustManager(tlsCert).build()).overrideAuthority(fabricProperties.tlsOverrideAuthority());
            }
            grpcChannel = grpcChannelBuilder.build();

            Gateway.Builder builder = Gateway.newInstance()
                    .identity(identity)
                    .signer(signer)
                    .connection(grpcChannel)
                    .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                    .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                    .submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                    .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));
            gateway = builder.connect();
            getCommands().forEach(commandManager::addCommand);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private List<Command> getCommands() {
        return List.of(
                new InvokeEvaluateCommand(gateway),
                new InvokeSubmitCommand(gateway)
        );
    }

    @Override
    public void postStop() {
        if (gateway != null) {
            gateway.close();
        }
        if (grpcChannel != null) {
            try {
                grpcChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Logger.error(e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
