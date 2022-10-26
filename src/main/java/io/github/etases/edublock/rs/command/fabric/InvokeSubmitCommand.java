package io.github.etases.edublock.rs.command.fabric;

import io.github.etases.edublock.rs.api.Command;
import org.hyperledger.fabric.client.Gateway;
import org.tinylog.Logger;

import java.nio.charset.StandardCharsets;

public class InvokeSubmitCommand extends Command {
    private final Gateway gateway;

    public InvokeSubmitCommand(Gateway gateway) {
        super("invoke-evaluate");
        this.gateway = gateway;
    }

    @Override
    public void runCommand(String argument) {
        String[] split = argument.split(" ", 4);
        if (split.length != 4) {
            Logger.info("Usage: invoke-evaluate <channel> <contractName> <function> <args...>");
            return;
        }

        String channel = split[0];
        String contractName = split[1];
        String function = split[2];
        String[] args = split[3].split(" ");

        try {
            var network = gateway.getNetwork(channel);
            var contract = network.getContract(contractName);
            var result = contract.newProposal(function)
                    .addArguments(args)
                    .build()
                    .endorse()
                    .submitAsync();
            if (result.getStatus().isSuccessful()) {
                Logger.info("Result: {} ({})", new String(result.getResult(), StandardCharsets.UTF_8), result.getStatus().getCode());
            } else {
                Logger.error("Error: {}", result.getStatus().getCode());
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    @Override
    public String getDescription() {
        return "Invoke and submit a transaction";
    }
}
