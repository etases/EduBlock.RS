package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Classroom;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.internal.jwt.JwtUtil;
import io.github.etases.edublock.rs.model.input.ClassCreate;
import io.github.etases.edublock.rs.model.input.ClassUpdate;
import io.github.etases.edublock.rs.model.input.ProfileUpdate;
import io.github.etases.edublock.rs.model.output.AccountListResponse;
import io.github.etases.edublock.rs.model.output.ClassroomListResponse;
import io.github.etases.edublock.rs.model.output.ClassroomResponse;
import io.github.etases.edublock.rs.model.output.element.AccountOutput;
import io.github.etases.edublock.rs.model.output.Response;
import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class StaffHandler extends SimpleServerHandler {
    private final SessionFactory sessionFactory;

    @Inject
    public StaffHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/staff/role/list/<role>", new AccountListHandler().handler(), JwtHandler.Roles.STAFF);
        server.post("/staff/profile/update/<id>", new ProfileUpdateHandler().handler(), JwtHandler.Roles.STAFF);

        server.get("/staff/class/list", new ClassListHandler().handler(), JwtHandler.Roles.STAFF);
        server.post("/staff/class/update/<id>", new ClassUpdateHandler().handler(), JwtHandler.Roles.STAFF);
        server.post("/staff/class/create", new CreateClassHandler().handler(), JwtHandler.Roles.STAFF);
    }

    private class AccountListHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("List accounts by role");
                        operation.description("List accounts with a specific role");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", AccountListResponse.class, builder -> builder.description("The list of accounts"));
        }

        @Override
        public void handle(Context ctx) {
            try (var session = sessionFactory.openSession()) {
                String role = ctx.pathParam("role").toUpperCase();
                var query = session.createNamedQuery("Account.findByRole", Account.class).setParameter("role", role);

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

    private class ProfileUpdateHandler implements ContextHandler {
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

                JwtHandler.Roles role = JwtHandler.Roles.getRole(account.getRole());
                if (role == JwtHandler.Roles.ADMIN) {
                    ctx.status(403);
                    ctx.json(new Response(2, "You cannot update an admin account"));
                    return;
                } else if (role == JwtHandler.Roles.STAFF) {
                    DecodedJWT jwt = JwtUtil.getDecodedFromContext(ctx);
                    long userId = jwt.getClaim("id").asLong();
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

    private class ClassUpdateHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Update class");
                        operation.description("Update class");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(ClassUpdate.class)
                    .result("200", Response.class, builder -> builder.description("The result of the operation"))
                    .result("404", Response.class, builder -> builder.description("The class does not exist"));
        }

        @Override
        public void handle(Context ctx) {
            ClassUpdate input = ctx.bodyValidator(ClassUpdate.class)
                    .check(ClassUpdate::validate, "Invalid data")
                    .get();

            try (var session = sessionFactory.openSession()) {
                long classId = Long.parseLong(ctx.pathParam("id"));
                Classroom classroom = session.get(Classroom.class, classId);

                if (classroom == null) {
                    ctx.status(404);
                    ctx.json(new Response(1, "Class not found"));
                    return;
                }

                Transaction transaction = session.beginTransaction();
                classroom.setName(input.name());
                classroom.setGrade(input.grade());
                session.update(classroom);
                transaction.commit();

                ctx.json(new Response(0, "Class updated"));
            }
        }
    }

    private class CreateClassHandler implements ContextHandler {
        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Create class");
                        operation.description("Create class");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .body(ClassCreate.class, builder -> builder.description("The class to create"))
                    .result("200", ClassroomResponse.class, builder -> builder.description("The class has been created"))
                    .result("400", ClassroomResponse.class, builder -> builder.description("The class already exists"));
        }

        @Override
        public void handle(Context ctx) {
            ClassCreate input = ctx.bodyValidator(ClassCreate.class)
                    .check(ClassCreate::validate, "Invalid data")
                    .get();
            try (var session = sessionFactory.openSession()) {
                var checkClass = session.createNamedQuery("Classroom.findByName", Classroom.class)
                        .setParameter("name", input.name())
                        .uniqueResult();
                if (checkClass != null) {
                    ctx.status(400);
                    ctx.json(new ClassroomResponse(1, "Class already exists", null));
                    return;
                }
                Transaction transaction = session.beginTransaction();
                var classroom = new Classroom();
                classroom.setName(input.name());
                classroom.setGrade(input.grade());
                session.save(classroom);
                transaction.commit();
                var output = new ClassroomOutput(
                        classroom.getId(),
                        classroom.getName(),
                        classroom.getGrade()
                );
                ctx.json(new ClassroomResponse(0, "Class created", output));
            }
        }
    }

    private class ClassListHandler implements ContextHandler {
        @Override
        public void handle(Context ctx) {
            try (var session = sessionFactory.openSession()) {
                var query = session.createNamedQuery("Classroom.findAll", Classroom.class);
                var classrooms = query.getResultList();
                List<ClassroomOutput> list = new ArrayList<>();
                for (var classroom : classrooms) {
                    list.add(new ClassroomOutput(
                            classroom.getId(),
                            classroom.getName(),
                            classroom.getGrade()
                    ));
                }
                ctx.json(new ClassroomListResponse(0, "Get classroom list", list));
            }
        }

        @Override
        public OpenApiDocumentation document() {
            return OpenApiBuilder.document()
                    .operation(operation -> {
                        operation.summary("Get class list");
                        operation.description("Get class list");
                        operation.addTagsItem("Staff");
                    })
                    .operation(SwaggerHandler.addSecurity())
                    .result("200", ClassroomListResponse.class, builder -> builder.description("The list of classroom"));
        }
    }
}
