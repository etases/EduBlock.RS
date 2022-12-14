package io.github.etases.edublock.rs.internal.student;

import com.google.gson.Gson;
import io.github.etases.edublock.rs.api.StudentUpdater;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.model.fabric.Record;
import io.github.etases.edublock.rs.model.fabric.*;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.Network;
import org.tinylog.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class FabricStudentUpdater implements StudentUpdater {
    private final Gson gson = new Gson();
    private final MainConfig mainConfig;
    private final Gateway gateway;
    private Network network;
    private Contract contract;

    private static boolean isNotKnownResponse(GatewayException exception) {
        for (var errorDetail : exception.getDetails()) {
            if (errorDetail.getMessage().contains("ASSET_NOT_FOUND")) {
                return false;
            }
        }
        return true;
    }

    private Contract getContract() {
        var properties = mainConfig.getFabricUpdaterProperties();
        if (network == null) {
            network = gateway.getNetwork(properties.channelName());
        }
        if (contract == null) {
            contract = network.getContract(properties.chaincodeName());
        }
        return contract;
    }

    @Override
    public CompletableFuture<Personal> getStudentPersonal(long studentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var result = getContract().evaluateTransaction("getStudentPersonal", Long.toString(studentId));
                return gson.fromJson(new String(result, StandardCharsets.UTF_8), Personal.class);
            } catch (GatewayException e) {
                if (isNotKnownResponse(e)) {
                    Logger.error(e);
                }
                return null;
            } catch (Exception e) {
                Logger.error(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> updateStudentPersonal(long studentId, Personal personal) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var result = getContract()
                        .newProposal("updateStudentPersonal")
                        .addArguments(Long.toString(studentId))
                        .putTransient("personal", gson.toJson(personal))
                        .build()
                        .endorse()
                        .submitAsync();
                return result.getStatus().isSuccessful();
            } catch (Exception e) {
                Logger.error(e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Record> getStudentRecord(long studentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var result = getContract().evaluateTransaction("getStudentRecord", Long.toString(studentId));
                return gson.fromJson(new String(result, StandardCharsets.UTF_8), Record.class);
            } catch (GatewayException e) {
                if (isNotKnownResponse(e)) {
                    Logger.error(e);
                }
                return null;
            } catch (Exception e) {
                Logger.error(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> updateStudentRecord(long studentId, Record record) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var result = getContract()
                        .newProposal("updateStudentRecord")
                        .addArguments(Long.toString(studentId))
                        .putTransient("record", gson.toJson(record))
                        .build()
                        .endorse()
                        .submitAsync();
                return result.getStatus().isSuccessful();
            } catch (GatewayException e) {
                Logger.error(e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> updateStudentClassRecord(long studentId, long classId, ClassRecord classRecord) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var result = getContract()
                        .newProposal("updateStudentClassRecord")
                        .addArguments(Long.toString(studentId), Long.toString(classId))
                        .putTransient("classRecord", gson.toJson(classRecord))
                        .build()
                        .endorse()
                        .submitAsync();
                return result.getStatus().isSuccessful();
            } catch (GatewayException e) {
                Logger.error(e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<List<RecordHistory>> getStudentRecordHistory(long studentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var result = getContract().evaluateTransaction("getStudentRecordHistory", Long.toString(studentId));
                return gson.fromJson(new String(result, StandardCharsets.UTF_8), RecordHistoryList.class).getHistories();
            } catch (Exception e) {
                Logger.error(e);
                return Collections.emptyList();
            }
        });
    }

    @Override
    public CompletableFuture<Map<Long, Personal>> getAllStudentPersonal() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var result = getContract().evaluateTransaction("getAllStudentPersonals");
                return gson.fromJson(new String(result, StandardCharsets.UTF_8), PersonalMap.class).getPersonals();
            } catch (Exception e) {
                Logger.error(e);
                return Collections.emptyMap();
            }
        });
    }

    @Override
    public CompletableFuture<Map<Long, Record>> getAllStudentRecord() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var result = getContract().evaluateTransaction("getAllStudentRecords");
                return gson.fromJson(new String(result, StandardCharsets.UTF_8), RecordMap.class).getRecords();
            } catch (Exception e) {
                Logger.error(e);
                return Collections.emptyMap();
            }
        });
    }
}
