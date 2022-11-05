package io.github.etases.edublock.rs.api;

import io.github.etases.edublock.rs.model.fabric.ClassRecord;
import io.github.etases.edublock.rs.model.fabric.Personal;
import io.github.etases.edublock.rs.model.fabric.Record;
import io.github.etases.edublock.rs.model.fabric.RecordHistory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StudentUpdater {
    CompletableFuture<Personal> getStudentPersonal(long studentId);

    CompletableFuture<Boolean> updateStudentPersonal(long studentId, Personal personal);

    CompletableFuture<Record> getStudentRecord(long studentId);

    CompletableFuture<Boolean> updateStudentRecord(long studentId, Record record);

    CompletableFuture<Boolean> updateStudentClassRecord(long studentId, long classId, ClassRecord classRecord);

    CompletableFuture<List<RecordHistory>> getStudentRecordHistory(long studentId);

    default void start() {
        // do nothing
    }

    default void stop() {
        // do nothing
    }
}
