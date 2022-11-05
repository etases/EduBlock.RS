package io.github.etases.edublock.rs.internal.student;

import io.github.etases.edublock.rs.api.StudentUpdater;
import io.github.etases.edublock.rs.model.fabric.ClassRecord;
import io.github.etases.edublock.rs.model.fabric.Personal;
import io.github.etases.edublock.rs.model.fabric.Record;
import io.github.etases.edublock.rs.model.fabric.RecordHistory;
import lombok.RequiredArgsConstructor;
import org.tinylog.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class StudentUpdaterWithLogger implements StudentUpdater {
    private final StudentUpdater studentUpdater;

    @Override
    public CompletableFuture<Personal> getStudentPersonal(long studentId) {
        Logger.info("getStudentPersonal({})", studentId);
        return studentUpdater.getStudentPersonal(studentId);
    }

    @Override
    public CompletableFuture<Boolean> updateStudentPersonal(long studentId, Personal personal) {
        Logger.info("updateStudentPersonal({}, {})", studentId, personal);
        return studentUpdater.updateStudentPersonal(studentId, personal);
    }

    @Override
    public CompletableFuture<Record> getStudentRecord(long studentId) {
        Logger.info("getStudentRecord({})", studentId);
        return studentUpdater.getStudentRecord(studentId);
    }

    @Override
    public CompletableFuture<Boolean> updateStudentRecord(long studentId, Record record) {
        Logger.info("updateStudentRecord({}, {})", studentId, record);
        return studentUpdater.updateStudentRecord(studentId, record);
    }

    @Override
    public CompletableFuture<Boolean> updateStudentClassRecord(long studentId, long classId, ClassRecord classRecord) {
        Logger.info("updateStudentClassRecord({}, {}, {})", studentId, classId, classRecord);
        return studentUpdater.updateStudentClassRecord(studentId, classId, classRecord);
    }

    @Override
    public CompletableFuture<List<RecordHistory>> getStudentRecordHistory(long studentId) {
        Logger.info("getStudentRecordHistory({})", studentId);
        return studentUpdater.getStudentRecordHistory(studentId);
    }
}
