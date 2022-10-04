package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.PasswordUtils;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.Student;
import io.github.etases.edublock.rs.model.input.*;
import io.github.etases.edublock.rs.model.output.*;
import io.github.etases.edublock.rs.model.output.element.AccountWithProfileOutput;
import io.github.etases.edublock.rs.model.output.element.AccountWithStudentProfileOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AccountHandler extends SimpleServerHandler {
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
        server.post("/account/list", this::bulkCreate, JwtHandler.Role.ADMIN);
        server.put("/account/list/password", this::bulkUpdatePassword, JwtHandler.Role.ADMIN);
        server.get("/account/role/{role}/list", this::listByRole, JwtHandler.Role.ADMIN, JwtHandler.Role.STAFF);
        server.get("/account/{id}", this::get, JwtHandler.Role.TEACHER, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
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
            pathParams = @OpenApiParam(name = "id", description = "The account id"),
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
            security = @OpenApiSecurity(name = SwaggerHandler.AUTH_KEY),
            responses = @OpenApiResponse(
                    status = "200",
                    description = "The list of accounts",
                    content = @OpenApiContent(from = AccountWithProfileListResponse.class)
            )
    )
    private void list(Context ctx) {
        try (var session = sessionFactory.openSession()) {
            var query = session.createNamedQuery("Account.findAll", Account.class);
            var accounts = query.getResultList();
            List<AccountWithProfileOutput> list = new ArrayList<>();
            for (var account : accounts) {
                list.add(AccountWithProfileOutput.fromEntity(account, id -> Profile.getOrDefault(session, id)));
            }
            ctx.json(new AccountWithProfileListResponse(0, "Get account list", list));
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
                            description = "The errors of the accounts. Should be empty",
                            content = @OpenApiContent(from = AccountCreateErrorListResponse.class)
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
            List<ResponseWithData<AccountCreate>> errors = new ArrayList<>();
            Transaction transaction = session.beginTransaction();
            for (var accountCreate : input.accounts()) {
                var optionalRole = JwtHandler.Role.getRoleOptional(accountCreate.role());
                if (optionalRole.isEmpty()) {
                    errors.add(new ResponseWithData<>(1, "Invalid role", accountCreate));
                    continue;
                }
                var role = optionalRole.get();
                String username = accountCreate.getUsername();
                String password = mainConfig.getDefaultPassword();
                long count = session.createNamedQuery("Account.countByUsernameRegex", Long.class)
                        .setParameter("username", username + "%")
                        .uniqueResult();
                String salt = PasswordUtils.generateSalt();
                String hashedPassword = PasswordUtils.hashPassword(password, salt);
                var account = new Account();
                account.setUsername(username + (count == 0 ? "" : count));
                account.setSalt(salt);
                account.setHashedPassword(hashedPassword);
                account.setRole(role.name().toUpperCase());
                session.save(account);
                var profile = new Profile();
                profile.setId(account.getId());
                profile.setAccount(account);
                profile.setFirstName(accountCreate.firstName());
                profile.setLastName(accountCreate.lastName());
                profile.setMale(true);
                profile.setAvatar("");
                profile.setBirthDate(Date.from(Instant.EPOCH));
                profile.setAddress("");
                profile.setPhone("");
                profile.setEmail("");
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
            }
            if (errors.isEmpty()) {
                transaction.commit();
                ctx.json(new AccountCreateErrorListResponse(0, "Bulk create account successfully", errors));
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
            List<ResponseWithData<AccountLogin>> errors = new ArrayList<>();
            Transaction transaction = session.beginTransaction();
            for (var accountInput : input.accounts()) {
                var account = session.createNamedQuery("Account.findByUsername", Account.class)
                        .setParameter("username", accountInput.username())
                        .uniqueResult();
                if (account == null) {
                    errors.add(new ResponseWithData<>(1, "Username does not exist", accountInput));
                    continue;
                }
                String salt = PasswordUtils.generateSalt();
                String hashedPassword = PasswordUtils.hashPassword(accountInput.password(), salt);
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
            path = "/account/role/{role}/list",
            methods = HttpMethod.GET,
            summary = "List accounts by role. Roles: ADMIN, STAFF",
            description = "List accounts with a specific role. Roles: ADMIN, STAFF",
            tags = {"Account"},
            pathParams = @OpenApiParam(name = "role", description = "The role of the accounts"),
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
        var optionalRole = JwtHandler.Role.getRoleOptional(ctx.pathParam("role"));
        if (optionalRole.isEmpty()) {
            ctx.status(400);
            ctx.json(new AccountWithProfileListResponse(1, "Invalid role", null));
            return;
        }
        var role = optionalRole.get();
        try (var session = sessionFactory.openSession()) {
            if (role == JwtHandler.Role.STUDENT) {
                var query = session.createNamedQuery("Student.findAll", Student.class);
                var accounts = query.getResultList();
                List<AccountWithStudentProfileOutput> list = new ArrayList<>();
                for (var account : accounts) {
                    list.add(AccountWithStudentProfileOutput.fromEntity(account, id -> Profile.getOrDefault(session, id)));
                }
                ctx.json(new AccountWithStudentProfileListResponse(0, "Get account list", list));
            } else {
                var query = session.createNamedQuery("Account.findByRole", Account.class).setParameter("role", role.name());
                var accounts = query.getResultList();
                List<AccountWithProfileOutput> list = new ArrayList<>();
                for (var account : accounts) {
                    list.add(AccountWithProfileOutput.fromEntity(account, id -> Profile.getOrDefault(session, id)));
                }
                ctx.json(new AccountWithProfileListResponse(0, "Get account list", list));
            }
        }
    }

    @OpenApi(
            path = "/account/{id}/profile",
            methods = HttpMethod.PUT,
            summary = "Update user profile. Roles: STAFF",
            description = "Update user profile. Roles: STAFF",
            tags = "Account",
            pathParams = @OpenApiParam(name = "id", description = "The id of the account"),
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
        ProfileUpdate input = ctx.bodyValidator(ProfileUpdate.class)
                .check(ProfileUpdate::validate, "Invalid data")
                .get();

        try (var session = sessionFactory.openSession()) {
            long accountId = Long.parseLong(ctx.pathParam("id"));
            Account account = session.get(Account.class, accountId);

            if (account == null) {
                ctx.status(404);
                ctx.json(new Response(1, "Account not found"));
                return;
            }

            JwtHandler.Role role = JwtHandler.Role.getRole(account.getRole());
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

            Profile profile = session.get(Profile.class, account.getId());

            if (profile == null) {
                ctx.status(404);
                ctx.json(new Response(3, "Profile not found"));
                return;
            }

            Transaction transaction = session.beginTransaction();
            profile.setFirstName(input.firstName());
            profile.setLastName(input.lastName());
            profile.setMale(input.male());
            profile.setAvatar(input.avatar());
            profile.setBirthDate(input.birthDate());
            profile.setAddress(input.address());
            profile.setPhone(input.phone());
            profile.setEmail(input.email());
            session.update(profile);
            transaction.commit();
            ctx.json(new Response(0, "Profile updated"));
        }
    }

    @OpenApi(
            path = "/account/{id}/student",
            methods = HttpMethod.PUT,
            summary = "Update student information. Roles: STAFF",
            description = "Update student information. Roles: STAFF",
            tags = "Account",
            pathParams = @OpenApiParam(name = "id", description = "The id of the account"),
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
            student.setEthnic(input.ethnic());
            student.setFatherName(input.fatherName());
            student.setFatherJob(input.fatherJob());
            student.setMotherName(input.motherName());
            student.setMotherJob(input.motherJob());
            student.setGuardianName(input.guardianName());
            student.setGuardianJob(input.guardianJob());
            student.setHomeTown(input.homeTown());
            session.update(student);
            transaction.commit();
            ctx.json(new Response(0, "Student updated"));
        }
    }
}
