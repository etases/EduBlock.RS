package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.RequestServer;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.api.StudentUpdater;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.RecordEntry;
import io.github.etases.edublock.rs.entity.Student;
import io.github.etases.edublock.rs.internal.classification.ClassificationManager;
import io.github.etases.edublock.rs.internal.student.FabricStudentUpdater;
import io.github.etases.edublock.rs.internal.student.LocalStudentUpdater;
import io.github.etases.edublock.rs.internal.student.StudentUpdaterWithLogger;
import io.github.etases.edublock.rs.internal.student.TemporaryStudentUpdater;
import io.github.etases.edublock.rs.internal.subject.SubjectManager;
import io.github.etases.edublock.rs.model.fabric.ClassRecord;
import io.github.etases.edublock.rs.model.fabric.Personal;
import io.github.etases.edublock.rs.model.fabric.Record;
import io.github.etases.edublock.rs.model.fabric.Subject;
import io.github.etases.edublock.rs.model.output.AccountWithStudentProfileResponse;
import io.github.etases.edublock.rs.model.output.RecordHistoryResponse;
import io.github.etases.edublock.rs.model.output.RecordListResponse;
import io.github.etases.edublock.rs.model.output.element.AccountWithStudentProfileOutput;
import io.github.etases.edublock.rs.model.output.element.RecordHistoryOutput;
import io.github.etases.edublock.rs.model.output.element.RecordOutput;
import io.javalin.http.Context;
import io.javalin.openapi.*;
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
    private RequestServer requestServer;
    @Inject
    private SessionFactory sessionFactory;
    @Inject
    private ServerBuilder serverBuilder;
    @Inject
    private MainConfig mainConfig;
    @Getter
    private StudentUpdater studentUpdater;
    private ScheduledExecutorService executorService;

    @Override
    public void postSetup() {
        var gateway = requestServer.getHandler(FabricHandler.class).getGateway();
        if (mainConfig.getDatabaseProperties().isMemory()) {
            studentUpdater = new TemporaryStudentUpdater();
        } else if (gateway == null) {
            studentUpdater = new LocalStudentUpdater();
        } else {
            studentUpdater = new FabricStudentUpdater(mainConfig, gateway);
        }

        if (mainConfig.getServerProperties().devMode()) {
            studentUpdater = new StudentUpdaterWithLogger(studentUpdater);
        }

        studentUpdater.start();

        serverBuilder.addHandler(javalin -> {
            javalin.get("/updater/{id}/personal", this::getPersonal);
            javalin.get("/updater/{id}/record", this::getRecord);
            javalin.get("/updater/{id}/history", this::getHistory);
        });

        var updaterPeriod = Math.max(mainConfig.getUpdaterPeriod(), 1);
        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(() -> {
            var current = currentFutureRef.get();
            if (current != null && !current.isDone()) return;
            if (updateRecords.get()) {
                updateRecords.set(false);
                currentFutureRef.set(updateRecord().thenAccept(v -> Logger.info("Updated records")));
            } else {
                updateRecords.set(true);
                currentFutureRef.set(updatePersonal().thenAccept(v -> Logger.info("Updated personal")));
            }
            Logger.info("Student update scheduled");
        }, updaterPeriod, updaterPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdown();
        }
        if (studentUpdater != null) {
            studentUpdater.stop();
        }
    }

    @OpenApi(
            path = "/updater/{id}/personal",
            methods = HttpMethod.GET,
            summary = "Get student personal.",
            description = "Get student personal.",
            tags = "Updater",
            pathParams = @OpenApiParam(name = "id", description = "The account id", required = true),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The student personal",
                            content = @OpenApiContent(from = AccountWithStudentProfileResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Personal not found",
                            content = @OpenApiContent(from = AccountWithStudentProfileResponse.class)
                    ),
            }
    )
    private void getPersonal(Context ctx) {
        var id = Long.parseLong(ctx.pathParam("id"));
        ctx.future(() -> studentUpdater.getStudentPersonal(id).thenAccept(personal -> {
            if (personal == null) {
                ctx.status(404);
                ctx.json(new AccountWithStudentProfileResponse(1, "Personal not found", null));
                return;
            }
            ctx.json(new AccountWithStudentProfileResponse(0, "Get personal", AccountWithStudentProfileOutput.fromFabricModel(id, personal)));
        }));
    }

    @OpenApi(
            path = "/updater/{id}/record",
            methods = HttpMethod.GET,
            summary = "Get student record.",
            description = "Get student record.",
            tags = "Updater",
            pathParams = @OpenApiParam(name = "id", description = "The account id", required = true),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The student record",
                            content = @OpenApiContent(from = RecordListResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Record not found",
                            content = @OpenApiContent(from = RecordListResponse.class)
                    ),
            }
    )
    private void getRecord(Context ctx) {
        var id = Long.parseLong(ctx.pathParam("id"));
        ctx.future(() -> studentUpdater.getStudentRecord(id).thenAccept(record -> {
            if (record == null) {
                ctx.status(404);
                ctx.json(new RecordListResponse(1, "Record not found", null));
                return;
            }
            ctx.json(new RecordListResponse(0, "OK", RecordOutput.fromFabricModel(record)));
        }));
    }

    @OpenApi(
            path = "/updater/{id}/history",
            methods = HttpMethod.GET,
            summary = "Get student record history.",
            description = "Get student record history.",
            tags = "Updater",
            pathParams = @OpenApiParam(name = "id", description = "The account id", required = true),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The student record history",
                            content = @OpenApiContent(from = RecordHistoryResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Record history not found",
                            content = @OpenApiContent(from = RecordHistoryResponse.class)
                    ),
            }
    )
    private void getHistory(Context ctx) {
        var id = Long.parseLong(ctx.pathParam("id"));
        ctx.future(() -> studentUpdater.getStudentRecordHistory(id).thenAccept(history -> {
            if (history == null) {
                ctx.status(404);
                ctx.json(new RecordHistoryResponse(1, "Record history not found", null));
                return;
            }
            var output = history.stream().map(RecordHistoryOutput::fromFabricModel).toList();
            ctx.json(new RecordHistoryResponse(0, "OK", output));
        }));
    }

    private CompletableFuture<Void> updatePersonal() {
        List<CompletableFuture<Void>> futures = new LinkedList<>();
        Map<Long, Personal> personalMap = new HashMap<>();
        Map<Long, Runnable> completeRunnableMap = new HashMap<>();

        var session = sessionFactory.openSession();
        var transaction = session.beginTransaction();

        var profiles = session.createNamedQuery("Profile.findUpdated", Profile.class).getResultList();
        for (var profile : profiles) {
            var student = session.get(Student.class, profile.getId());
            var completeRunnable = (Runnable) () -> {
                profile.setUpdated(false);
                session.update(profile);
            };
            if (student != null) {
                var personal = Personal.fromEntity(student, profile);
                personalMap.put(student.getId(), personal);
                completeRunnableMap.put(student.getId(), completeRunnable);
            } else {
                completeRunnable.run();
            }
        }

        personalMap.forEach((id, personal) -> futures.add(studentUpdater.updateStudentPersonal(id, personal).thenAccept(success -> {
            if (mainConfig.getServerProperties().devMode()) {
                Logger.info("Updated personal: " + id + " " + success);
            }
            if (Boolean.TRUE.equals(success)) {
                completeRunnableMap.getOrDefault(id, () -> {
                }).run();
            }
        })));
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
            transaction.commit();
            session.close();
        });
    }

    private CompletableFuture<Boolean> updateRecord(long studentId, Map<Long, List<RecordEntry>> recordsPerClassMap) {
        if (recordsPerClassMap.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> {
                if (mainConfig.getServerProperties().devMode()) {
                    Logger.info("Updated record: " + studentId + " " + true);
                }
                return true;
            });
        }
        return studentUpdater.getStudentRecord(studentId)
                .thenApply(Record::clone)
                .thenCompose(record -> {
                    var classRecords = record.getClassRecords();

                    for (var entry : recordsPerClassMap.entrySet()) {
                        var classId = entry.getKey();
                        var recordEntries = new ArrayList<>(entry.getValue());
                        recordEntries.sort(Comparator.comparing(RecordEntry::getApprovalDate));

                        var classRecord = classRecords.getOrDefault(classId, ClassRecord.clone(null));
                        var updateSubjectMap = classRecord.getSubjects();

                        // Update class record
                        boolean updateClass = true;
                        for (var recordEntry : recordEntries) {
                            // Update Subject
                            var subjectId = recordEntry.getSubjectId();
                            var updateSubject = updateSubjectMap.getOrDefault(subjectId, Subject.clone(null));
                            updateSubject.setName(SubjectManager.getSubject(subjectId).getIdentifier());
                            updateSubject.setFirstHalfScore(recordEntry.getFirstHalfScore());
                            updateSubject.setSecondHalfScore(recordEntry.getSecondHalfScore());
                            updateSubject.setFinalScore(recordEntry.getFinalScore());
                            updateSubjectMap.put(subjectId, updateSubject);

                            // Update Class
                            if (updateClass) {
                                var classroom = recordEntry.getRecord().getClassroom();
                                classRecord.setClassName(classroom.getName());
                                classRecord.setYear(classroom.getYear());
                                classRecord.setGrade(classroom.getGrade());
                                updateClass = false;
                            }
                        }

                        // Update classification
                        var classification = classRecord.getClassification();
                        Map<Long, Float> subjectFirstHalfScores = new HashMap<>();
                        Map<Long, Float> subjectSecondHalfScores = new HashMap<>();
                        Map<Long, Float> subjectFinalScores = new HashMap<>();
                        updateSubjectMap.forEach((subjectId, subject) -> {
                            subjectFirstHalfScores.put(subjectId, subject.getFirstHalfScore());
                            subjectSecondHalfScores.put(subjectId, subject.getSecondHalfScore());
                            subjectFinalScores.put(subjectId, subject.getFinalScore());
                        });
                        classification.setFirstHalfClassify(ClassificationManager.classifyRawSubjectMap(subjectFirstHalfScores).getIdentifier());
                        classification.setSecondHalfClassify(ClassificationManager.classifyRawSubjectMap(subjectSecondHalfScores).getIdentifier());
                        classification.setFinalClassify(ClassificationManager.classifyRawSubjectMap(subjectFinalScores).getIdentifier());

                        classRecords.put(classId, classRecord);
                    }

                    record.setClassRecords(classRecords);
                    return studentUpdater.updateStudentRecord(studentId, record);
                })
                .thenApply(success -> {
                    if (mainConfig.getServerProperties().devMode()) {
                        Logger.info("Updated record: " + studentId + " " + success);
                    }
                    return success;
                });
    }

    private CompletableFuture<Void> updateRecord() {
        Map<Long, Map<Long, List<RecordEntry>>> recordsPerStudentMap = new HashMap<>();
        Map<Long, List<Runnable>> recordRunnablePerStudentMap = new HashMap<>();

        var session = sessionFactory.openSession();
        var transaction = session.beginTransaction();

        var recordEntries = session.createNamedQuery("RecordEntry.findNeedUpdate", RecordEntry.class).getResultList();
        for (var record : recordEntries) {
            var studentId = record.getRecord().getStudent().getId();
            var classId = record.getRecord().getClassroom().getId();
            var recordsPerClassMap = recordsPerStudentMap.computeIfAbsent(studentId, k -> new HashMap<>());
            var recordEntriesPerClass = recordsPerClassMap.computeIfAbsent(classId, k -> new ArrayList<>());
            recordEntriesPerClass.add(record);

            var recordRunnables = recordRunnablePerStudentMap.computeIfAbsent(studentId, k -> new ArrayList<>());
            recordRunnables.add(() -> {
                record.setUpdateComplete(true);
                session.update(record);
            });
        }

        List<CompletableFuture<Void>> futures = new LinkedList<>();
        recordsPerStudentMap.forEach((id, map) -> futures.add(updateRecord(id, map).thenAccept(success -> {
            if (Boolean.TRUE.equals(success)) {
                recordRunnablePerStudentMap.getOrDefault(id, Collections.emptyList()).forEach(Runnable::run);
            }
        })));
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
            transaction.commit();
            session.close();
        });
    }
}
