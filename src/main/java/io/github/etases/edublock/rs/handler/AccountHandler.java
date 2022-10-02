package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.PasswordUtils;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
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
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
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
        server.get("/account", new GetHandler(true).handler(), JwtHandler.Role.authenticated());
        server.get("/account/list", new ListHandler().handler(), JwtHandler.Role.ADMIN, JwtHandler.Role.STAFF);
        server.post("/account/list", new BulkCreateAccountHandler().handler(), JwtHandler.Role.ADMIN);
        server.put("/account/list/password", new BulkUpdateAccountPasswordHandler().handler(), JwtHandler.Role.ADMIN);
        server.get("/account/role/<role>/list", new ListByRoleHandler().handler(), JwtHandler.Role.ADMIN, JwtHandler.Role.STAFF);
        server.get("/account/<id>", new GetHandler(false).handler(), JwtHandler.Role.TEACHER, JwtHandler.Role.STAFF, JwtHandler.Role.ADMIN);
        server.put("/account/<id>/profile", new UpdateProfileHandler().handler(), JwtHandler.Role.STAFF);
        server.put("/account/<id>/student", new UpdateStudentHandler().handler(), JwtHandler.Role.STAFF);
    }

    private class GetHandler implements ContextHandler {
        private final boolean isOwnOnly;

        private GetHandler(boolean isOwnOnly) {
            this.isOwnOnly = isOwnOnly;
        }

        @Override
        public void handle(Context ctx) {
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

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get account");
                        operation.description("Get account");
                        operation.addTagsItem("Account");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", AccountWithProfileResponse.class, builder -> builder.description("The account"))
                    .result("200", AccountWithStudentProfileResponse.class, builder -> builder.description("The student account"))
                    .result("404", AccountWithProfileResponse.class, builder -> builder.description("Account not found"));
        }
    }

    private class ListHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("List all accounts");
                        operation.description("List all accounts");
                        operation.addTagsItem("Account");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", AccountWithProfileListResponse.class, builder -> builder.description("The list of accounts"));
        }

        @Override
        public void handle(Context ctx) {
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
    }

    private class BulkCreateAccountHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(SwaggerHandler.addSecurity())
                    .operation(operation -> {
                        operation.summary("Create multiple accounts");
                        operation.description("Create multiple accounts");
                        operation.addTagsItem("Account");
                    })
                    .body(AccountCreateListInput.class,
                            builder -> builder.description("The list of accounts to create"))
                    .result("200", AccountCreateErrorListResponse.class,
                            builder -> builder.description("The errors of the accounts. Should be empty"))
                    .result("400", AccountCreateErrorListResponse.class,
                            builder -> builder.description("The errors of the accounts."));
        }

        @Override
        public void handle(Context ctx) {
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
                        student.setMotherName("");
                        student.setFatherJob("");
                        student.setMotherJob("");
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
    }

    private class BulkUpdateAccountPasswordHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Update multiple accounts password");
                        operation.description("Update multiple accounts password");
                        operation.addTagsItem("Account");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(AccountLoginListInput.class, builder -> builder.description("The list of accounts to update"))
                    .result("200", AccountLoginErrorListResponse.class,
                            builder -> builder.description("The errors of the accounts. Should be empty"))
                    .result("400", AccountLoginErrorListResponse.class,
                            builder -> builder.description("The errors of the accounts."));
        }

        @Override
        public void handle(Context ctx) {
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
    }

    private class ListByRoleHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("List accounts by role");
                        operation.description("List accounts with a specific role");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", AccountWithProfileListResponse.class, builder -> builder.description("The list of accounts"))
                    .result("200", AccountWithStudentProfileListResponse.class, builder -> builder.description("The list of student accounts"))
                    .result("400", AccountWithProfileListResponse.class, builder -> builder.description("Invalid role"));
        }

        @Override
        public void handle(Context ctx) {
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
    }

    private class UpdateProfileHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Update user profile");
                        operation.description("Update user profile");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(ProfileUpdate.class)
                    .result("200", Response.class, builder -> builder.description("The result of the operation"))
                    .result("404", Response.class, builder -> builder.description("The account does not exist"));
        }

        @Override
        public void handle(Context ctx) {
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
    }

    private class UpdateStudentHandler implements ContextHandler {
        @Override
        public void handle(Context ctx) {
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

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Update student information");
                        operation.description("Update student information");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(StudentUpdate.class)
                    .result("200", Response.class, builder -> builder.description("The result of the operation"))
                    .result("404", Response.class, builder -> builder.description("The student does not exist"));
        }
    }
}
