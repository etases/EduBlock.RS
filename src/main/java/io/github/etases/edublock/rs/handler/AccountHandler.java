package io.github.etases.edublock.rs.handler;

import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.model.output.AccountListResponse;
import io.github.etases.edublock.rs.model.output.AccountOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public class AccountHandler extends SimpleServerHandler {
    private final SessionFactory sessionFactory;

    @Inject
    public AccountHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/account/list", new AccountListHandler().handler(), JwtHandler.Roles.ADMIN);
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
                            account.getRole()
                    ));
                }
                ctx.json(new AccountListResponse(0, "Get account list", list));
            }
        }
    }
}
