package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.api.StudentUpdater;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.RecordEntry;
import io.github.etases.edublock.rs.entity.Student;
import io.github.etases.edublock.rs.internal.student.TemporaryStudentUpdater;
import io.github.etases.edublock.rs.model.fabric.ClassRecord;
import io.github.etases.edublock.rs.model.fabric.Personal;
import io.github.etases.edublock.rs.model.fabric.Record;
import io.github.etases.edublock.rs.model.fabric.Subject;
import lombok.Getter;
import org.hibernate.SessionFactory;
import org.tinylog.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class StudentUpdateHandler implements ServerHandler {
    private final AtomicReference<CompletableFuture<Void>> currentFutureRef = new AtomicReference<>();
    private final AtomicBoolean updateRecords = new AtomicBoolean(false);
    @Inject
    private SessionFactory sessionFactory;
    @Getter
    private StudentUpdater studentUpdater;
    private ScheduledExecutorService executorService;

    @Override
    public void postSetup() {
        studentUpdater = new TemporaryStudentUpdater();
        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(() -> {
            var current = currentFutureRef.get();
            if (current != null && current.isDone()) return;
            if (updateRecords.get()) {
                updateRecords.set(false);
                currentFutureRef.set(updateRecord().thenAccept(v -> Logger.info("Updated records")));
            } else {
                updateRecords.set(true);
                currentFutureRef.set(updatePersonal().thenAccept(v -> Logger.info("Updated personal")));
            }
            Logger.info("Student update scheduled");
        }, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private CompletableFuture<Void> updatePersonal() {
        List<CompletableFuture<Void>> futures = new LinkedList<>();
        Map<Long, Personal> personalMap = new HashMap<>();
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var profiles = session.createNamedQuery("Profile.findUpdated", Profile.class).getResultList();
            for (var profile : profiles) {
                var student = session.get(Student.class, profile.getId());
                if (student != null) {
                    var personal = Personal.fromEntity(student, profile);
                    personalMap.put(student.getId(), personal);
                }
                profile.setUpdated(false);
                session.update(profile);
            }
            transaction.commit();
        }
        personalMap.forEach((id, personal) -> futures.add(studentUpdater.updateStudentPersonal(id, personal).thenAccept(success -> {
        })));
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> updateRecord(long studentId, Map<Long, List<RecordEntry>> recordsPerClassMap) {
        return studentUpdater.getStudentRecord(studentId)
                .thenApply(Record::clone)
                .thenCompose(record -> {
                    var classRecords = record.getClassRecords();

                    for (var entry : recordsPerClassMap.entrySet()) {
                        var classId = entry.getKey();
                        var recordEntries = new ArrayList<>(entry.getValue());
                        recordEntries.sort(Comparator.comparing(RecordEntry::getApprovalDate));

                        var classRecord = classRecords.getOrDefault(classId, ClassRecord.clone(null));
                        var subjects = classRecord.getSubjects();

                        boolean updateClass = true;
                        for (var recordEntry : recordEntries) {
                            // Update Subject
                            var subjectId = recordEntry.getSubject().getId();
                            var outSubject = subjects.getOrDefault(subjectId, Subject.clone(null));
                            outSubject.setName(recordEntry.getSubject().getName());
                            outSubject.setFirstHalfScore(recordEntry.getFirstHalfScore());
                            outSubject.setSecondHalfScore(recordEntry.getSecondHalfScore());
                            outSubject.setFinalScore(recordEntry.getFinalScore());
                            subjects.put(subjectId, outSubject);

                            // Update Class
                            if (updateClass) {
                                var classroom = recordEntry.getRecord().getClassroom();
                                classRecord.setClassName(classroom.getName());
                                classRecord.setYear(classroom.getYear());
                                classRecord.setGrade(classroom.getGrade());
                                updateClass = false;
                            }
                        }

                        classRecords.put(classId, classRecord);
                    }

                    record.setClassRecords(classRecords);
                    return studentUpdater.updateStudentRecord(studentId, record);
                })
                .thenAccept(success -> {
                });
    }

    private CompletableFuture<Void> updateRecord() {
        Map<Long, Map<Long, List<RecordEntry>>> recordsPerStudentMap = new HashMap<>();
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var recordEntries = session.createNamedQuery("RecordEntry.findNeedUpdate", RecordEntry.class).getResultList();
            for (var record : recordEntries) {
                var studentId = record.getRecord().getStudent().getId();
                var classId = record.getRecord().getClassroom().getId();
                var recordsPerClassMap = recordsPerStudentMap.computeIfAbsent(studentId, k -> new HashMap<>());
                var recordEntriesPerClass = recordsPerClassMap.computeIfAbsent(classId, k -> new ArrayList<>());
                recordEntriesPerClass.add(record);
                record.setUpdateComplete(true);
                session.update(record);
            }
            transaction.commit();
        }
        List<CompletableFuture<Void>> futures = new LinkedList<>();
        recordsPerStudentMap.forEach((id, map) -> futures.add(updateRecord(id, map)));
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
