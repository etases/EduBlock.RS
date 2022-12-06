package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.Student;
import io.github.etases.edublock.rs.internal.account.AccountUtil;
import io.github.etases.edublock.rs.internal.account.PasswordUtil;
import io.github.etases.edublock.rs.internal.filter.ListSessionInputFilter;
import io.github.etases.edublock.rs.internal.pagination.PaginationUtil;
import io.github.etases.edublock.rs.model.input.*;
import io.github.etases.edublock.rs.model.output.*;
import io.github.etases.edublock.rs.model.output.element.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import io.javalin.security.RouteRole;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AccountHandler extends SimpleServerHandler {
    private static final ListSessionInputFilter<Account> ACCOUNTS_FILTER = ListSessionInputFilter.<Account>create()
            .addFilter("id", (input, o) -> Long.toString(o.getId()).equals(input))
            .addFilter("username", (input, o) -> o.getUsername().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("firstname", (session, input, o) -> Profile.getOrDefault(session, o.getId()).getFirstName().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("lastname", (session, input, o) -> Profile.getOrDefault(session, o.getId()).getLastName().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("email", (session, input, o) -> Profile.getOrDefault(session, o.getId()).getEmail().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("phone", (session, input, o) -> Profile.getOrDefault(session, o.getId()).getPhone().contains(input))
            .addFilter("year", (input, o) -> {
                Calendar calendar = Calendar.getInstance();
                if (input == null || input.isEmpty()) {
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    input = Integer.toString(calendar.get(Calendar.YEAR));
                }
                calendar.setTime(o.getCreatedAt());
                return Integer.toString(calendar.get(Calendar.YEAR)).equals(input);
            });
    private static final ListSessionInputFilter<Student> STUDENTS_FILTER = ListSessionInputFilter.<Student>create()
            .addFilter("id", (input, o) -> Long.toString(o.getId()).equals(input))
            .addFilter("username", (input, o) -> o.getAccount().getUsername().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("firstname", (session, input, o) -> Profile.getOrDefault(session, o.getId()).getFirstName().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("lastname", (session, input, o) -> Profile.getOrDefault(session, o.getId()).getLastName().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("email", (session, input, o) -> Profile.getOrDefault(session, o.getId()).getEmail().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)))
            .addFilter("phone", (session, input, o) -> Profile.getOrDefault(session, o.getId()).getPhone().contains(input))
            .addFilter("year", (input, o) -> {
                Calendar calendar = Calendar.getInstance();
                if (input == null || input.isEmpty()) {
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    input = Integer.toString(calendar.get(Calendar.YEAR));
                }
                calendar.setTime(o.getAccount().getCreatedAt());
                return Integer.toString(calendar.get(Calendar.YEAR)).equals(input);
            });
    private final SessionFactory sessionFactory;
    private final MainConfig mainConfig;

    @Inject
    public AccountHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory, MainConfig mainConfig) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
        this.mainConfig = mainConfig;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/account", this::getOwn, JwtHandler.Role.authenticated());
        server.get("/account/list", this::list, JwtHandler.Role.ADMIN, JwtHandler.Role.STAFF);
        server.post("/account/list", this::bulkCreate,
                mainConfig.getServerProperties().devMode()
                        ? new RouteRole[0]
                        : new RouteRole[]{JwtHandler.Role.ADMIN}
        );
        server.put("/account/list/password", this::bulkUpdatePassword, JwtHandler.Role.ADMIN);
        server.put("/account/password", this::updatePassword, JwtHandler.Role.authenticated());
        server.get("/account/role/{role}/list", this::listByRole, JwtHandler.Role.ADMIN, JwtHandler.Role.STAFF);
        server.get("/account/{id}", this::get, JwtHandler.Role.TEACHER, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
        server.put("/account/self/profile", this::updateSelfProfile, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
        server.put("/account/{id}/profile", this::updateProfile, JwtHandler.Role.STAFF);
        server.put("/account/{id}/student", this::updateStudent, JwtHandler.Role.STAFF);
    }

    private void get(Context ctx, boolean isOwnOnly) {
        long userId = isOwnOnly ? JwtHandler.getUserId(ctx) : Long.parseLong(ctx.pathParam("id"));
        try (var session = sessionFactory.openSession()) {
            var account = session.get(Account.class, userId);
            if (account == null) {
                ctx.status(404);
                ctx.json(new AccountWithProfileResponse(1, "Account not found", null));
                return;
            }
            if (JwtHandler.Role.getRole(account.getRole()) == JwtHandler.Role.STUDENT) {
                var student = session.get(Student.class, userId);
                var output = AccountWithStudentProfileOutput.fromEntity(student, id -> Profile.getOrDefault(session, id));
                ctx.json(new AccountWithStudentProfileResponse(0, "Get account", output));
            } else {
                var output = AccountWithProfileOutput.fromEntity(account, id -> Profile.getOrDefault(session, id));
                ctx.json(new AccountWithProfileResponse(0, "Get account", output));
            }
        }
    }

    @OpenApi(
            path = "/account",
            methods = HttpMethod.GET,
            summary = "Get account (own)",
            description = "Get account (own)",
            tags = "Account",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The account",
                            content = @OpenApiContent(from = AccountWithProfileResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "200",
                            description = "The student account",
                            content = @OpenApiContent(from = AccountWithStudentProfileResponse.class)
                    ),
            }
    )
    private void getOwn(Context ctx) {
        get(ctx, true);
    }

    @OpenApi(
            path = "/account/{id}",
            methods = HttpMethod.GET,
            summary = "Get account. Roles: TEACHER, STAFF, ADMIN",
            description = "Get account. Roles: TEACHER, STAFF, ADMIN",
            tags = "Account",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            pathParams = @OpenApiParam(name = "id", description = "The account id", required = true),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The account",
                            content = @OpenApiContent(from = AccountWithProfileResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "200",
                            description = "The student account",
                            content = @OpenApiContent(from = AccountWithStudentProfileResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Account not found",
                            content = @OpenApiContent(from = AccountWithProfileResponse.class)
                    ),
            }
    )
    private void get(Context ctx) {
        get(ctx, false);
    }

    @OpenApi(
            path = "/account/list",
            methods = HttpMethod.GET,
            summary = "List all accounts. Roles: ADMIN, STAFF",
            description = "List all accounts. Roles: ADMIN, STAFF",
            tags = "Account",
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size"),
                    @OpenApiParam(name = "filter", description = "Filter Type"),
                    @OpenApiParam(name = "input", description = "Filter Input"),
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    description = "The list of accounts",
                    content = @OpenApiContent(from = AccountWithProfileListResponse.class)
            )
    )
    private void list(Context ctx) {
        var paginationParameter = PaginationParameter.fromQuery(ctx);
        try (var session = sessionFactory.openSession()) {
            var query = session.createNamedQuery("Account.findAll", Account.class);
            var accounts = query.getResultList();
            var filtered = ACCOUNTS_FILTER.filter(session, accounts, ctx);
            var pagedPair = PaginationUtil.getPagedList(filtered, paginationParameter);
            List<AccountWithProfileOutput> list = new ArrayList<>();
            for (var account : pagedPair.getKey()) {
                list.add(AccountWithProfileOutput.fromEntity(account, id -> Profile.getOrDefault(session, id)));
            }
            ctx.json(new AccountWithProfileListResponse(0, "Get account list", pagedPair.getValue(), list));
        }
    }

    @OpenApi(
            path = "/account/list",
            methods = HttpMethod.POST,
            summary = "Create multiple accounts. Roles: ADMIN",
            description = "Create multiple accounts. Roles: ADMIN",
            tags = "Account",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AccountCreateListInput.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The list of the accounts.",
                            content = @OpenApiContent(from = AccountWithProfileListResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "The errors of the accounts.",
                            content = @OpenApiContent(from = AccountCreateErrorListResponse.class)
                    ),
            }
    )
    private void bulkCreate(Context ctx) {
        AccountCreateListInput input = ctx.bodyValidator(AccountCreateListInput.class)
                .check(AccountCreateListInput::validate, "Invalid account list")
                .get();

        try (var session = sessionFactory.openSession()) {
            List<AccountCreateErrorListResponse.ErrorData> errors = new ArrayList<>();
            List<AccountWithProfileOutput> outputs = new ArrayList<>();
            Transaction transaction = session.beginTransaction();
            for (var accountCreate : input.getAccounts()) {
                var optionalRole = JwtHandler.Role.getRoleOptional(accountCreate.getRole());
                if (optionalRole.isEmpty()) {
                    errors.add(new AccountCreateErrorListResponse.ErrorData(1, "Invalid role", accountCreate));
                    continue;
                }
                var role = optionalRole.get();
                String username = AccountUtil.generateUsername(accountCreate.getFirstName(), accountCreate.getLastName());
                String password = mainConfig.getDefaultPassword();
                var account = AccountUtil.createAccount(session, username, password);
                account.setRole(role.name().toUpperCase());
                session.save(account);
                var profile = new Profile();
                profile.setId(account.getId());
                profile.setAccount(account);
                profile.setFirstName(accountCreate.getFirstName());
                profile.setLastName(accountCreate.getLastName());
                profile.setMale(true);
                profile.setAvatar("");
                profile.setBirthDate(Date.from(Instant.EPOCH));
                profile.setAddress("");
                profile.setPhone("");
                profile.setEmail("");
                profile.setUpdated(true);
                session.save(profile);
                if (role == JwtHandler.Role.STUDENT) {
                    var student = new Student();
                    student.setId(account.getId());
                    student.setAccount(account);
                    student.setEthnic("");
                    student.setFatherName("");
                    student.setFatherJob("");
                    student.setMotherName("");
                    student.setMotherJob("");
                    student.setGuardianName("");
                    student.setGuardianJob("");
                    student.setHomeTown("");
                    session.save(student);
                }
                outputs.add(new AccountWithProfileOutput(AccountOutput.fromEntity(account), ProfileOutput.fromEntity(profile)));
            }
            if (errors.isEmpty()) {
                transaction.commit();
                ctx.json(new AccountWithProfileListResponse(0, "Bulk create account successfully", PaginationInfo.whole(outputs), outputs));
            } else {
                transaction.rollback();
                ctx.status(400);
                ctx.json(new AccountCreateErrorListResponse(1, "There are errors in the account list", errors));
            }
        }
    }

    @OpenApi(
            path = "/account/list/password",
            methods = HttpMethod.PUT,
            summary = "Update multiple accounts password. Roles: ADMIN",
            description = "Update multiple accounts password. Roles: ADMIN",
            tags = "Account",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AccountLoginListInput.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The errors of the accounts. Should be empty",
                            content = @OpenApiContent(from = AccountLoginErrorListResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "The errors of the accounts.",
                            content = @OpenApiContent(from = AccountLoginErrorListResponse.class)
                    ),
            }
    )
    private void bulkUpdatePassword(Context ctx) {
        AccountLoginListInput input = ctx.bodyValidator(AccountLoginListInput.class)
                .check(AccountLoginListInput::validate, "Invalid account list")
                .get();

        try (var session = sessionFactory.openSession()) {
            List<AccountLoginErrorListResponse.ErrorData> errors = new ArrayList<>();
            Transaction transaction = session.beginTransaction();
            for (var accountInput : input.getAccounts()) {
                var account = session.createNamedQuery("Account.findByUsername", Account.class)
                        .setParameter("username", accountInput.getUsername())
                        .uniqueResult();
                if (account == null) {
                    errors.add(new AccountLoginErrorListResponse.ErrorData(1, "Username does not exist", accountInput));
                    continue;
                }
                String salt = PasswordUtil.generateSalt();
                String hashedPassword = PasswordUtil.hashPassword(accountInput.getPassword(), salt);
                account.setSalt(salt);
                account.setHashedPassword(hashedPassword);
                session.update(account);
            }
            if (errors.isEmpty()) {
                transaction.commit();
                ctx.json(new AccountLoginErrorListResponse(0, "Bulk update account password successfully", errors));
            } else {
                transaction.rollback();
                ctx.status(400);
                ctx.json(new AccountLoginErrorListResponse(1, "There are errors in the account list", errors));
            }
        }
    }

    @OpenApi(
            path = "/account/password",
            methods = HttpMethod.PUT,
            summary = "Update password (own)",
            description = "Update password (own)",
            tags = "Account",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = UpdatePasswordInput.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Success",
                            content = @OpenApiContent(from = Response.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "Invalid old password",
                            content = @OpenApiContent(from = Response.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Account not found",
                            content = @OpenApiContent(from = Response.class)
                    ),
            }
    )
    private void updatePassword(Context ctx) {
        UpdatePasswordInput input = ctx.bodyValidator(UpdatePasswordInput.class)
                .check(UpdatePasswordInput::validate, "Invalid input")
                .get();
        long userId = JwtHandler.getUserId(ctx);
        try (var session = sessionFactory.openSession()) {
            var account = session.get(Account.class, userId);

            if (account == null) {
                ctx.status(404);
                ctx.json(new Response(1, "Account does not exist"));
                return;
            }

            if (!PasswordUtil.verifyPassword(input.getOldPassword(), account.getSalt(), account.getHashedPassword())) {
                ctx.status(400);
                ctx.json(new Response(2, "Old password is incorrect"));
                return;
            }

            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hashPassword(input.getNewPassword(), salt);

            Transaction transaction = session.beginTransaction();
            account.setSalt(salt);
            account.setHashedPassword(hashedPassword);
            session.update(account);
            transaction.commit();

            ctx.json(new Response(0, "Update password successfully"));
        }
    }

    @OpenApi(
            path = "/account/role/{role}/list",
            methods = HttpMethod.GET,
            summary = "List accounts by role. Roles: ADMIN, STAFF",
            description = "List accounts with a specific role. Roles: ADMIN, STAFF",
            tags = {"Account"},
            pathParams = @OpenApiParam(name = "role", description = "The role of the accounts", required = true),
            queryParams = {
                    @OpenApiParam(name = "pageNumber", description = "Page number"),
                    @OpenApiParam(name = "pageSize", description = "Page size"),
                    @OpenApiParam(name = "filter", description = "Filter Type"),
                    @OpenApiParam(name = "input", description = "Filter Input"),
            },
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = AccountWithProfileListResponse.class),
                            description = "The list of accounts"
                    ),
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = AccountWithStudentProfileListResponse.class),
                            description = "The list of student accounts"
                    ),
                    @OpenApiResponse(
                            status = "400",
                            content = @OpenApiContent(from = AccountWithProfileListResponse.class),
                            description = "Invalid role"
                    )
            }
    )
    private void listByRole(Context ctx) {
        var paginationParameter = PaginationParameter.fromQuery(ctx);
        var optionalRole = JwtHandler.Role.getRoleOptional(ctx.pathParam("role"));
        if (optionalRole.isEmpty()) {
            ctx.status(400);
            ctx.json(new AccountWithProfileListResponse(1, "Invalid role", null, null));
            return;
        }
        var role = optionalRole.get();
        try (var session = sessionFactory.openSession()) {
            if (role == JwtHandler.Role.STUDENT) {
                var query = session.createNamedQuery("Student.findAll", Student.class);
                var accounts = query.getResultList();
                var filtered = STUDENTS_FILTER.filter(session, accounts, ctx);
                var pagedPair = PaginationUtil.getPagedList(filtered, paginationParameter);
                List<AccountWithStudentProfileOutput> list = new ArrayList<>();
                for (var account : pagedPair.getKey()) {
                    list.add(AccountWithStudentProfileOutput.fromEntity(account, id -> Profile.getOrDefault(session, id)));
                }
                ctx.json(new AccountWithStudentProfileListResponse(0, "Get account list", pagedPair.getValue(), list));
            } else {
                var query = session.createNamedQuery("Account.findByRole", Account.class).setParameter("role", role.name());
                var accounts = query.getResultList();
                var filtered = ACCOUNTS_FILTER.filter(session, accounts, ctx);
                var pagedPair = PaginationUtil.getPagedList(filtered, paginationParameter);
                List<AccountWithProfileOutput> list = new ArrayList<>();
                for (var account : pagedPair.getKey()) {
                    list.add(AccountWithProfileOutput.fromEntity(account, id -> Profile.getOrDefault(session, id)));
                }
                ctx.json(new AccountWithProfileListResponse(0, "Get account list", pagedPair.getValue(), list));
            }
        }
    }

    private void updateProfile(Context ctx, long accountId, boolean bypassCheck) {
        ProfileUpdate input = ctx.bodyValidator(ProfileUpdate.class)
                .check(ProfileUpdate::validate, "Invalid data")
                .get();

        try (var session = sessionFactory.openSession()) {
            Account account = session.get(Account.class, accountId);

            if (account == null) {
                ctx.status(404);
                ctx.json(new Response(1, "Account not found"));
                return;
            }

            JwtHandler.Role role = JwtHandler.Role.getRole(account.getRole());
            if (!bypassCheck) {
                if (role == JwtHandler.Role.ADMIN) {
                    ctx.status(403);
                    ctx.json(new Response(2, "You cannot update an admin account"));
                    return;
                } else if (role == JwtHandler.Role.STAFF) {
                    long userId = JwtHandler.getUserId(ctx);
                    if (userId != accountId) {
                        ctx.status(403);
                        ctx.json(new Response(3, "You cannot update another staff account"));
                        return;
                    }
                }
            }

            Profile profile = session.get(Profile.class, account.getId());

            if (profile == null) {
                ctx.status(404);
                ctx.json(new Response(3, "Profile not found"));
                return;
            }

            Transaction transaction = session.beginTransaction();
            profile.setFirstName(input.getFirstName());
            profile.setLastName(input.getLastName());
            profile.setMale(input.isMale());
            profile.setAvatar(input.getAvatar());
            profile.setBirthDate(input.getBirthDate());
            profile.setAddress(input.getAddress());
            profile.setPhone(input.getPhone());
            profile.setEmail(input.getEmail());
            profile.setUpdated(true);
            session.update(profile);
            transaction.commit();
            ctx.json(new Response(0, "Profile updated"));
        }
    }

    @OpenApi(
            path = "/account/{id}/profile",
            methods = HttpMethod.PUT,
            summary = "Update user profile. Roles: STAFF",
            description = "Update user profile. Roles: STAFF",
            tags = "Account",
            pathParams = @OpenApiParam(name = "id", description = "The id of the account", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ProfileUpdate.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The result of the operation",
                            content = @OpenApiContent(from = Response.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "The account does not exist",
                            content = @OpenApiContent(from = Response.class)
                    ),
            }
    )
    private void updateProfile(Context ctx) {
        updateProfile(ctx, Long.parseLong(ctx.pathParam("id")), false);
    }

    @OpenApi(
            path = "/account/self/profile",
            methods = HttpMethod.PUT,
            summary = "Update self profile. Roles: STAFF, ADMIN",
            description = "Update self profile. Roles: STAFF, ADMIN",
            tags = "Account",
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ProfileUpdate.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The result of the operation",
                            content = @OpenApiContent(from = Response.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "The account does not exist",
                            content = @OpenApiContent(from = Response.class)
                    ),
            }
    )
    private void updateSelfProfile(Context ctx) {
        updateProfile(ctx, JwtHandler.getUserId(ctx), true);
    }

    @OpenApi(
            path = "/account/{id}/student",
            methods = HttpMethod.PUT,
            summary = "Update student information. Roles: STAFF",
            description = "Update student information. Roles: STAFF",
            tags = "Account",
            pathParams = @OpenApiParam(name = "id", description = "The id of the account", required = true),
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = StudentUpdate.class)),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The result of the operation",
                            content = @OpenApiContent(from = Response.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "The student does not exist",
                            content = @OpenApiContent(from = Response.class)
                    ),
            }
    )
    private void updateStudent(Context ctx) {
        StudentUpdate input = ctx.bodyValidator(StudentUpdate.class)
                .check(StudentUpdate::validate, "Invalid data")
                .get();

        try (var session = sessionFactory.openSession()) {
            long studentId = Long.parseLong(ctx.pathParam("id"));
            Student student = session.get(Student.class, studentId);

            if (student == null) {
                ctx.status(404);
                ctx.json(new Response(1, "Student not found"));
                return;
            }

            Transaction transaction = session.beginTransaction();

            student.setEthnic(input.getEthnic());
            student.setFatherName(input.getFatherName());
            student.setFatherJob(input.getFatherJob());
            student.setMotherName(input.getMotherName());
            student.setMotherJob(input.getMotherJob());
            student.setGuardianName(input.getGuardianName());
            student.setGuardianJob(input.getGuardianJob());
            student.setHomeTown(input.getHomeTown());
            session.update(student);

            Profile profile = session.get(Profile.class, student.getId());
            profile.setUpdated(true);
            session.update(profile);

            transaction.commit();
            ctx.json(new Response(0, "Student updated"));
        }
    }
}
