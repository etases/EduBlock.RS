package io.github.etases.edublock.rs.internal.student;

import io.github.etases.edublock.rs.api.StudentUpdater;
import io.github.etases.edublock.rs.model.fabric.ClassRecord;
import io.github.etases.edublock.rs.model.fabric.Personal;
import io.github.etases.edublock.rs.model.fabric.Record;
import io.github.etases.edublock.rs.model.fabric.RecordHistory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TemporaryStudentUpdater implements StudentUpdater {
    protected final Map<Long, Personal> personalMap = new ConcurrentHashMap<>();
    protected final Map<Long, List<RecordHistory>> recordHistoryMap = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Personal> getStudentPersonal(long studentId) {
        return CompletableFuture.completedFuture(personalMap.get(studentId));
    }

    @Override
    public CompletableFuture<Boolean> updateStudentPersonal(long studentId, Personal personal) {
        personalMap.put(studentId, personal);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Record> getStudentRecord(long studentId) {
        var recordHistories = recordHistoryMap.get(studentId);
        if (recordHistories == null || recordHistories.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return recordHistories.stream().max(Comparator.comparing(RecordHistory::getTimestamp))
                .map(RecordHistory::getRecord)
                .map(CompletableFuture::completedFuture)
                .orElse(CompletableFuture.completedFuture(null));
    }

    @Override
    public CompletableFuture<Boolean> updateStudentRecord(long studentId, Record record) {
        var recordHistory = new RecordHistory();
        recordHistory.setRecord(record);
        recordHistory.setTimestamp(new Date());
        recordHistory.setUpdatedBy("system");
        recordHistoryMap.computeIfAbsent(studentId, k -> new ArrayList<>()).add(recordHistory);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateStudentClassRecord(long studentId, long classId, ClassRecord classRecord) {
        return getStudentRecord(studentId).thenCompose(record -> {
            if (record == null) {
                record = new Record();
            }
            var newRecord = new Record();
            newRecord.setClassRecords(new HashMap<>(record.getClassRecords()));
            newRecord.getClassRecords().put(classId, classRecord);
            return updateStudentRecord(studentId, record);
        });
    }

    @Override
    public CompletableFuture<List<RecordHistory>> getStudentRecordHistory(long studentId) {
        return CompletableFuture.completedFuture(recordHistoryMap.getOrDefault(studentId, Collections.emptyList()));
    }
}
