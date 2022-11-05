package io.github.etases.edublock.rs.internal.student;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.etases.edublock.rs.model.fabric.Personal;
import io.github.etases.edublock.rs.model.fabric.RecordHistory;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class LocalStudentUpdater extends TemporaryStudentUpdater {
    private final Gson gson = new Gson();
    private final File localPersonalFile;
    private final File localRecordHistoryFile;

    public LocalStudentUpdater(File localPersonalFile, File localRecordHistoryFile) {
        this.localPersonalFile = localPersonalFile;
        this.localRecordHistoryFile = localRecordHistoryFile;
    }

    public LocalStudentUpdater() {
        this(new File("personal.json"), new File("recordHistory.json"));
    }

    @Override
    public void start() {
        super.start();
        if (localPersonalFile.exists()) {
            Type type = new TypeToken<Map<Long, Personal>>() {
            }.getType();
            try (var reader = new FileReader(localPersonalFile)) {
                personalMap.putAll(gson.fromJson(reader, type));
            } catch (Exception e) {
                Logger.error(e, "Failed to load personal from {}", localPersonalFile);
            }
        }
        if (localRecordHistoryFile.exists()) {
            Type type = new TypeToken<Map<Long, List<RecordHistory>>>() {
            }.getType();
            try (var reader = new FileReader(localRecordHistoryFile)) {
                recordHistoryMap.putAll(gson.fromJson(reader, type));
            } catch (Exception e) {
                Logger.error(e, "Failed to load record history from {}", localRecordHistoryFile);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();

        // Create file if not exists
        if (!localPersonalFile.exists()) {
            try {
                if (localPersonalFile.createNewFile()) {
                    Logger.info("Created personal file {}", localPersonalFile);
                }
            } catch (Exception e) {
                Logger.error(e, "Failed to create personal file {}", localPersonalFile);
            }
        }
        if (!localRecordHistoryFile.exists()) {
            try {
                if (localRecordHistoryFile.createNewFile()) {
                    Logger.info("Created record history file {}", localRecordHistoryFile);
                }
            } catch (Exception e) {
                Logger.error(e, "Failed to create record history file {}", localRecordHistoryFile);
            }
        }

        // Write to file
        try (var writer = new FileWriter(localPersonalFile)) {
            gson.toJson(personalMap, writer);
        } catch (Exception e) {
            Logger.error(e, "Failed to save personal to {}", localPersonalFile);
        }
        try (var writer = new FileWriter(localRecordHistoryFile)) {
            gson.toJson(recordHistoryMap, writer);
        } catch (Exception e) {
            Logger.error(e, "Failed to save record history to {}", localRecordHistoryFile);
        }
    }
}
