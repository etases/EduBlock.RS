package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.PasswordUtils;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.config.MainConfig;
import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.model.input.AccountCreate;
import io.github.etases.edublock.rs.model.input.AccountCreateListInput;
import io.github.etases.edublock.rs.model.input.AccountLogin;
import io.github.etases.edublock.rs.model.input.AccountLoginListInput;
import io.github.etases.edublock.rs.model.output.*;
import io.github.etases.edublock.rs.model.output.element.AccountOutput;
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
        server.get("/account/list", new AccountListHandler().handler(), JwtHandler.Roles.ADMIN);
        server.post("/account/list", new BulkCreateAccountHandler().handler(), JwtHandler.Roles.ADMIN);
        server.put("/account/list/password", new BulkUpdateAccountPasswordHandler().handler(), JwtHandler.Roles.ADMIN);
    }

    private class AccountListHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", AccountListResponse.class, builder -> builder.description("The list of accounts"));
        }

        @Override
        public void handle(Context ctx) {
            try (var session = sessionFactory.openSession()) {
                var query = session.createNamedQuery("Account.findAll", Account.class);
                var accounts = query.getResultList();
                List<AccountOutput> list = new ArrayList<>();
                for (var account : accounts) {
                    var profile = session.get(Profile.class, account.getId());
                    if (profile == null) {
                        profile = new Profile();
                    }
                    list.add(new AccountOutput(
                            account.getId(),
                            account.getUsername(),
                            profile.getFirstName(),
                            profile.getLastName(),
                            profile.getAvatar(),
                            profile.getBirthDate(),
                            profile.getAddress(),
                            profile.getPhone(),
                            profile.getEmail(),
                            account.getRole()));
                }
                ctx.json(new AccountListResponse(0, "Get account list", list));
            }
        }
    }

    private class BulkCreateAccountHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(SwaggerHandler.addSecurity())
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
                    if (!JwtHandler.Roles.isValid(accountCreate.role())) {
                        errors.add(new ResponseWithData<>(1, "Invalid role", accountCreate));
                        continue;
                    }
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
                    account.setRole(accountCreate.role().toUpperCase());
                    session.save(account);
                    var profile = new Profile();
                    profile.setId(account.getId());
                    profile.setAccount(account);
                    profile.setFirstName(accountCreate.firstName());
                    profile.setLastName(accountCreate.lastName());
                    profile.setAvatar("");
                    profile.setBirthDate(Date.from(Instant.EPOCH));
                    profile.setAddress("");
                    profile.setPhone("");
                    profile.setEmail("");
                    session.save(profile);
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
}
