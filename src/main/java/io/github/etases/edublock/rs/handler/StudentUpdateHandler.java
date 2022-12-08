package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.CommandManager;
import io.github.etases.edublock.rs.RequestServer;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ServerHandler;
import io.github.etases.edublock.rs.api.StudentUpdater;
import io.github.etases.edublock.rs.command.updater.RestoreCommand;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.entity.*;
import io.github.etases.edublock.rs.internal.account.AccountUtil;
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
import io.github.etases.edublock.rs.model.input.StatisticKeyCreateInput;
import io.github.etases.edublock.rs.model.output.*;
import io.github.etases.edublock.rs.model.output.element.*;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.tinylog.Logger;

import java.sql.Date;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class StudentUpdateHandler implements ServerHandler {
    private final AtomicReference<CompletableFuture<Void>> currentFutureRef = new AtomicReference<>();
    @Inject
    private CommandManager commandManager;
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
            javalin.post("/updater", this::createNewKey, JwtHandler.Role.STUDENT);
            javalin.get("/updater/list", this::getKeyList, JwtHandler.Role.STUDENT);
            javalin.delete("/updater/{key}", this::deleteKey, JwtHandler.Role.STUDENT);
            javalin.get("/updater/{key}/personal", this::getPersonal);
            javalin.get("/updater/{key}/record", this::getRecord);
            javalin.get("/updater/{key}/history", this::getHistory);

            javalin.post("/statistic", this::createNewStatisticKey, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
            javalin.get("/statistic/list", this::getStatisticKeyList, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
            javalin.delete("/statistic/{key}", this::deleteStatisticKey, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
            javalin.get("/statistic/{key}", this::getStatistic);
        });

        var updaterPeriod = Math.max(mainConfig.getUpdaterPeriod(), 1);
        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(() -> {
            var current = currentFutureRef.get();
            if (current != null && !current.isDone()) return;
            currentFutureRef.set(
                    updateRecord()
                            .thenAccept(v -> Logger.info("Updated records"))
                            .thenCompose(v -> updatePersonal())
                            .thenAccept(v -> Logger.info("Updated personal"))
            );
            Logger.info("Student update scheduled");
        }, updaterPeriod, updaterPeriod, TimeUnit.SECONDS);

        commandManager.addCommand(new RestoreCommand(this));
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

    private Optional<UUID> getKey(Context ctx) {
        var rawKey = ctx.pathParam("key");
        UUID key;
        try {
            key = UUID.fromString(rawKey);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return Optional.of(key);
    }

    private Optional<UpdaterKey> getKey(Session session, Context ctx) {
        return getKey(ctx).map(key -> session.get(UpdaterKey.class, key.toString()));
    }

    private Optional<StatisticKey> getStatisticKey(Session session, Context ctx) {
        return getKey(ctx).map(key -> session.get(StatisticKey.class, key.toString()));
    }

    @OpenApi(
            path = "/updater",
            methods = HttpMethod.POST,
            summary = "Create new key. Roles: STUDENT",
            description = "Create new key. Roles: STUDENT",
            tags = "Updater",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The key",
                            content = @OpenApiContent(from = StringResponse.class)
                    )
            }
    )
    private void createNewKey(Context ctx) {
        long userId = JwtHandler.getUserId(ctx);
        try (var session = sessionFactory.openSession()) {
            var student = session.get(Student.class, userId);
            if (student == null) {
                ctx.status(404);
                ctx.json(new StringResponse(1, "Student not found", null));
                return;
            }

            Transaction transaction = session.beginTransaction();
            var updaterKey = new UpdaterKey();
            updaterKey.setStudent(student);
            session.save(updaterKey);
            transaction.commit();

            ctx.json(new StringResponse(0, "OK", updaterKey.getId()));
        }
    }

    @OpenApi(
            path = "/updater/list",
            methods = HttpMethod.GET,
            summary = "Get key list. Roles: STUDENT",
            description = "Get key list. Roles: STUDENT",
            tags = "Updater",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The key list",
                            content = @OpenApiContent(from = StringListResponse.class)
                    )
            }
    )
    private void getKeyList(Context ctx) {
        long userId = JwtHandler.getUserId(ctx);
        try (var session = sessionFactory.openSession()) {
            var student = session.get(Student.class, userId);
            if (student == null) {
                ctx.status(404);
                ctx.json(new StringListResponse(1, "Student not found", null));
                return;
            }

            var list = student.getUpdaterKey().stream().map(UpdaterKey::getId).toList();
            ctx.json(new StringListResponse(0, "OK", list));
        }
    }

    @OpenApi(
            path = "/updater/{key}",
            methods = HttpMethod.DELETE,
            summary = "Delete key. Roles: STUDENT",
            description = "Delete key. Roles: STUDENT",
            tags = "Updater",
            pathParams = @OpenApiParam(name = "key", description = "The account key", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Success",
                            content = @OpenApiContent(from = Response.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Key not found",
                            content = @OpenApiContent(from = Response.class)
                    )
            }
    )
    private void deleteKey(Context ctx) {
        long userId = JwtHandler.getUserId(ctx);
        try (var session = sessionFactory.openSession()) {
            var updaterKey = getKey(session, ctx).orElse(null);
            if (updaterKey == null || updaterKey.getStudent().getId() != userId) {
                ctx.status(404);
                ctx.json(new Response(1, "Key not found"));
                return;
            }
            Transaction transaction = session.beginTransaction();
            session.delete(updaterKey);
            transaction.commit();
            ctx.json(new Response(0, "OK"));
        }
    }

    @OpenApi(
            path = "/updater/{key}/personal",
            methods = HttpMethod.GET,
            summary = "Get student personal.",
            description = "Get student personal.",
            tags = "Updater",
            pathParams = @OpenApiParam(name = "key", description = "The account key", required = true),
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
                    )
            }
    )
    private void getPersonal(Context ctx) {
        long id;
        try (var session = sessionFactory.openSession()) {
            var updaterKey = getKey(session, ctx);
            if (updaterKey.isEmpty()) {
                ctx.status(404);
                ctx.json(new AccountWithStudentProfileResponse(1, "Key not found", null));
                return;
            }
            id = updaterKey.get().getStudent().getId();
        }

        ctx.future(() -> studentUpdater.getStudentPersonal(id).thenAccept(personal -> {
            if (personal == null) {
                ctx.status(404);
                ctx.json(new AccountWithStudentProfileResponse(2, "Personal not found", null));
                return;
            }
            ctx.json(new AccountWithStudentProfileResponse(0, "Get personal", AccountWithStudentProfileOutput.fromFabricModel(id, personal)));
        }));
    }

    @OpenApi(
            path = "/updater/{key}/record",
            methods = HttpMethod.GET,
            summary = "Get student record.",
            description = "Get student record.",
            tags = "Updater",
            pathParams = @OpenApiParam(name = "key", description = "The account key", required = true),
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
                    )
            }
    )
    private void getRecord(Context ctx) {
        long id;
        try (var session = sessionFactory.openSession()) {
            var updaterKey = getKey(session, ctx);
            if (updaterKey.isEmpty()) {
                ctx.status(404);
                ctx.json(new RecordListResponse(1, "Key not found", null));
                return;
            }
            id = updaterKey.get().getStudent().getId();
        }

        ctx.future(() -> studentUpdater.getStudentRecord(id).thenAccept(record -> {
            if (record == null) {
                ctx.status(404);
                ctx.json(new RecordListResponse(2, "Record not found", null));
                return;
            }
            ctx.json(new RecordListResponse(0, "OK", RecordOutput.fromFabricModel(record)));
        }));
    }

    @OpenApi(
            path = "/updater/{key}/history",
            methods = HttpMethod.GET,
            summary = "Get student record history.",
            description = "Get student record history.",
            tags = "Updater",
            pathParams = @OpenApiParam(name = "key", description = "The account key", required = true),
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
                    )
            }
    )
    private void getHistory(Context ctx) {
        long id;
        try (var session = sessionFactory.openSession()) {
            var updaterKey = getKey(session, ctx);
            if (updaterKey.isEmpty()) {
                ctx.status(404);
                ctx.json(new RecordHistoryResponse(1, "Key not found", null));
                return;
            }
            id = updaterKey.get().getStudent().getId();
        }

        ctx.future(() -> studentUpdater.getStudentRecordHistory(id).thenAccept(history -> {
            if (history == null) {
                ctx.status(404);
                ctx.json(new RecordHistoryResponse(2, "Record history not found", null));
                return;
            }
            var output = history.stream().map(RecordHistoryOutput::fromFabricModel).toList();
            ctx.json(new RecordHistoryResponse(0, "OK", output));
        }));
    }

    @OpenApi(
            path = "/statistic",
            methods = HttpMethod.POST,
            summary = "Create new statistic key. Roles: ADMIN, STAFF",
            description = "Create new statistic key. Roles: ADMIN, STAFF",
            tags = "Updater",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = StatisticKeyCreateInput.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The key",
                            content = @OpenApiContent(from = StringResponse.class)
                    )
            }
    )
    private void createNewStatisticKey(Context ctx) {
        StatisticKeyCreateInput input = ctx.bodyValidator(StatisticKeyCreateInput.class)
                .check(StatisticKeyCreateInput::validate, "Invalid input")
                .get();
        try (var session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            var key = new StatisticKey();
            key.setGrade(input.getGrade());
            key.setYear(input.getYear());
            session.save(key);
            transaction.commit();
            ctx.json(new StringResponse(0, "OK", key.getId()));
        }
    }

    @OpenApi(
            path = "/statistic/list",
            methods = HttpMethod.GET,
            summary = "Get statistic key list. Roles: ADMIN, STAFF",
            description = "Get statistic key list. Roles: ADMIN, STAFF",
            tags = "Updater",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The key list",
                            content = @OpenApiContent(from = StatisticKeyListResponse.class)
                    )
            }
    )
    private void getStatisticKeyList(Context ctx) {
        try (var session = sessionFactory.openSession()) {
            var keys = session.createNamedQuery("StatisticKey.findAll", StatisticKey.class).list();
            var output = keys.stream().map(StatisticKeyOutput::fromEntity).toList();
            ctx.json(new StatisticKeyListResponse(0, "OK", output));
        }
    }

    @OpenApi(
            path = "/statistic/{key}",
            methods = HttpMethod.DELETE,
            summary = "Delete statistic key. Roles: ADMIN, STAFF",
            description = "Delete statistic key. Roles: ADMIN, STAFF",
            tags = "Updater",
            pathParams = @OpenApiParam(name = "key", description = "The statistic key", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Success",
                            content = @OpenApiContent(from = Response.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Key not found",
                            content = @OpenApiContent(from = Response.class)
                    )
            }
    )
    private void deleteStatisticKey(Context ctx) {
        try (var session = sessionFactory.openSession()) {
            var key = getStatisticKey(session, ctx);
            if (key.isEmpty()) {
                ctx.status(404);
                ctx.json(new Response(1, "Key not found"));
                return;
            }
            Transaction transaction = session.beginTransaction();
            session.delete(key.get());
            transaction.commit();
            ctx.json(new Response(0, "OK"));
        }
    }

    @OpenApi(
            path = "/statistic/{key}",
            methods = HttpMethod.GET,
            summary = "Get records.",
            description = "Get records.",
            tags = "Updater",
            pathParams = @OpenApiParam(name = "key", description = "The statistic key", required = true),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The student records",
                            content = @OpenApiContent(from = RecordWithStudentListResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Key not found",
                            content = @OpenApiContent(from = RecordWithStudentListResponse.class)
                    )
            }
    )
    private void getStatistic(Context ctx) {
        StatisticKey key;
        try (var session = sessionFactory.openSession()) {
            var keyOpt = getStatisticKey(session, ctx);
            if (keyOpt.isEmpty()) {
                ctx.status(404);
                ctx.json(new RecordWithStudentListResponse(1, "Key not found", null));
                return;
            }
            key = keyOpt.get();
        }

        AtomicReference<Map<Long, Personal>> personalMapRef = new AtomicReference<>();
        AtomicReference<Map<Long, Record>> recordMapRef = new AtomicReference<>();
        CompletableFuture<Void> personalFuture = studentUpdater.getAllStudentPersonal().thenAccept(personalMapRef::set);
        CompletableFuture<Void> recordFuture = studentUpdater.getAllStudentRecord().thenAccept(recordMapRef::set);
        ctx.future(() -> CompletableFuture.allOf(personalFuture, recordFuture).thenAccept(v -> {
            var personalOutputMap = personalMapRef.get().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> AccountWithStudentProfileOutput.fromFabricModel(e.getKey(), e.getValue())));
            var recordOutputMap = recordMapRef.get().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> RecordOutput.fromFabricModel(e.getValue())));
            List<RecordWithStudentOutput> output = new ArrayList<>();
            for (var recordListEntry : recordOutputMap.entrySet()) {
                var recordList = recordListEntry.getValue();
                var personal = personalOutputMap.get(recordListEntry.getKey());
                if (personal == null) {
                    continue;
                }
                for (var record : recordList) {
                    if (record.getClassroom().getGrade() != key.getGrade() || record.getClassroom().getYear() != key.getYear()) {
                        continue;
                    }
                    output.add(new RecordWithStudentOutput(record.getClassroom(), record.getEntries(), record.getClassification(), personal));
                }
            }
            ctx.json(new RecordWithStudentListResponse(0, "OK", output));
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

    public CompletableFuture<Void> restoreData() {
        return studentUpdater.getAllStudentPersonal()
                .thenAccept(map -> {
                    try (var session = sessionFactory.openSession()) {
                        var transaction = session.beginTransaction();
                        for (Map.Entry<Long, Personal> entry : map.entrySet()) {
                            long id = entry.getKey();
                            var personal = entry.getValue();

                            Logger.info("Restoring student: " + id);

                            var account = session.get(Account.class, id);
                            if (account != null) {
                                Logger.warn("Cannot restore personal for student: " + id + " because account already exists");
                                continue;
                            }

                            var username = AccountUtil.generateUsername(personal.getFirstName(), personal.getLastName());
                            var password = mainConfig.getDefaultPassword();
                            account = AccountUtil.createAccount(session, username, password);
                            account.setId(id);
                            account.setRole(JwtHandler.Role.STUDENT.name());
                            session.save(account);

                            var profile = new Profile();
                            profile.setId(account.getId());
                            profile.setAccount(account);
                            profile.setFirstName(personal.getFirstName());
                            profile.setLastName(personal.getLastName());
                            profile.setMale(personal.isMale());
                            profile.setAvatar(personal.getAvatar());
                            profile.setBirthDate(personal.getBirthDate());
                            profile.setAddress(personal.getAddress());
                            profile.setPhone("");
                            profile.setEmail("");
                            profile.setUpdated(false);
                            session.save(profile);

                            var student = new Student();
                            student.setId(account.getId());
                            student.setAccount(account);
                            student.setEthnic(personal.getEthnic());
                            student.setFatherName(personal.getFatherName());
                            student.setFatherJob(personal.getFatherJob());
                            student.setMotherName(personal.getMotherName());
                            student.setMotherJob(personal.getMotherJob());
                            student.setGuardianName(personal.getGuardianName());
                            student.setGuardianJob(personal.getGuardianJob());
                            student.setHomeTown(personal.getHomeTown());
                            session.save(student);

                            Logger.info("Restored student: " + id);
                        }
                        transaction.commit();
                    }
                })
                .thenCompose(v -> studentUpdater.getAllStudentRecord())
                .thenAccept(map -> {
                    try (var session = sessionFactory.openSession()) {
                        var transaction = session.beginTransaction();
                        for (var recordMapEntry : map.entrySet()) {
                            long id = recordMapEntry.getKey();
                            var record = recordMapEntry.getValue();

                            Logger.info("Restoring record: " + id);

                            var student = session.get(Student.class, id);
                            if (student == null) {
                                Logger.warn("Cannot restore record for student: " + id + " because student does not exist");
                                continue;
                            }

                            for (var classRecordMapEntry : record.getClassRecords().entrySet()) {
                                long classroomId = classRecordMapEntry.getKey();
                                var classRecord = classRecordMapEntry.getValue();

                                Logger.info("Restoring class record: " + id + " " + classroomId);

                                var recordEntity = session.createNamedQuery("Record.findByStudentAndClassroom", io.github.etases.edublock.rs.entity.Record.class)
                                        .setParameter("studentId", id)
                                        .setParameter("classroomId", classroomId)
                                        .uniqueResult();
                                if (recordEntity == null) {
                                    Logger.info("Creating new record: " + id + " " + classroomId);

                                    var classroom = session.get(Classroom.class, classroomId);
                                    if (classroom == null) {
                                        Logger.info("Creating classroom: " + classroomId);

                                        classroom = new Classroom();
                                        classroom.setId(classroomId);
                                        classroom.setName(classRecord.getClassName());
                                        classroom.setYear(classRecord.getYear());
                                        classroom.setGrade(classRecord.getGrade());
                                        session.save(classroom);

                                        Logger.info("Created classroom: " + classroomId);
                                    }
                                    recordEntity = new io.github.etases.edublock.rs.entity.Record();
                                    recordEntity.setStudent(student);
                                    recordEntity.setClassroom(classroom);
                                    session.save(recordEntity);

                                    Logger.info("Created new record: " + id + " " + classroomId);
                                }

                                for (var subjectMapEntry : classRecord.getSubjects().entrySet()) {
                                    Long subjectId = subjectMapEntry.getKey();
                                    var subjectRecord = subjectMapEntry.getValue();

                                    Logger.info("Restoring subject record: " + id + " " + classroomId + " " + subjectId);

                                    if (SubjectManager.getSubject(subjectId) == null) {
                                        Logger.warn("Cannot restore record for student: " + id + " because subject does not exist");
                                        continue;
                                    }

                                    var recordEntry = new RecordEntry();
                                    recordEntry.setRecord(recordEntity);
                                    recordEntry.setSubjectId(subjectId);
                                    recordEntry.setFirstHalfScore(subjectRecord.getFirstHalfScore());
                                    recordEntry.setSecondHalfScore(subjectRecord.getSecondHalfScore());
                                    recordEntry.setFinalScore(subjectRecord.getFinalScore());
                                    recordEntry.setUpdateComplete(true);
                                    recordEntry.setRequestDate(Date.from(Instant.now()));
                                    recordEntry.setApprovalDate(Date.from(Instant.now()));
                                    session.save(recordEntry);

                                    Logger.info("Restored subject record: " + id + " " + classroomId + " " + subjectId);
                                }
                            }
                        }
                        transaction.commit();
                    }
                });
    }
}
