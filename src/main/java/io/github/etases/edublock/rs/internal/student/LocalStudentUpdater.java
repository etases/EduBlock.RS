package io.github.etases.edublock.rs.internal.student;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.etases.edublock.rs.internal.util.FileUtil;
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
    private final Type personalType = new TypeToken<Map<Long, Personal>>() {
    }.getType();
    private final Type recordHistoryType = new TypeToken<Map<Long, List<RecordHistory>>>() {
    }.getType();

    public LocalStudentUpdater(File localPersonalFile, File localRecordHistoryFile) {
        this.localPersonalFile = localPersonalFile;
        this.localRecordHistoryFile = localRecordHistoryFile;
    }

    public LocalStudentUpdater() {
        this(new File("updater", "personal.json"), new File("updater", "recordHistory.json"));
    }

    @Override
    public void start() {
        super.start();
        if (localPersonalFile.exists()) {
            try (var reader = new FileReader(localPersonalFile)) {
                personalMap.putAll(gson.fromJson(reader, personalType));
            } catch (Exception e) {
                Logger.error(e, "Failed to load personal from {}", localPersonalFile);
            }
        }
        if (localRecordHistoryFile.exists()) {
            try (var reader = new FileReader(localRecordHistoryFile)) {
                recordHistoryMap.putAll(gson.fromJson(reader, recordHistoryType));
            } catch (Exception e) {
                Logger.error(e, "Failed to load record history from {}", localRecordHistoryFile);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();

        // Create file if not exists
        try {
            if (!FileUtil.createFile(localPersonalFile)) {
                Logger.error("Failed to create personal file {}", localPersonalFile);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to create personal file {}", localPersonalFile);
        }
        try {
            if (!FileUtil.createFile(localRecordHistoryFile)) {
                Logger.error("Failed to create record history file {}", localRecordHistoryFile);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to create record history file {}", localRecordHistoryFile);
        }

        // Write to file
        try (var writer = new FileWriter(localPersonalFile)) {
            gson.toJson(personalMap, personalType, writer);
        } catch (Exception e) {
            Logger.error(e, "Failed to save personal to {}", localPersonalFile);
        }
        try (var writer = new FileWriter(localRecordHistoryFile)) {
            gson.toJson(recordHistoryMap, recordHistoryType, writer);
        } catch (Exception e) {
            Logger.error(e, "Failed to save record history to {}", localRecordHistoryFile);
        }
    }
}
