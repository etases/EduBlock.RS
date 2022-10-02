package io.github.etases.edublock.rs.handler;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import io.github.etases.edublock.rs.ServerBuilder;
import io.github.etases.edublock.rs.api.ContextHandler;
import io.github.etases.edublock.rs.api.SimpleServerHandler;
import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Classroom;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.Student;
import io.github.etases.edublock.rs.internal.jwt.JwtUtil;
import io.github.etases.edublock.rs.model.input.ClassCreate;
import io.github.etases.edublock.rs.model.input.ClassUpdate;
import io.github.etases.edublock.rs.model.input.ProfileUpdate;
import io.github.etases.edublock.rs.model.input.StudentUpdate;
import io.github.etases.edublock.rs.model.output.*;
import io.github.etases.edublock.rs.model.output.element.AccountWithProfileOutput;
import io.github.etases.edublock.rs.model.output.element.AccountWithStudentProfileOutput;
import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffHandler extends SimpleServerHandler {
    private final SessionFactory sessionFactory;

    @Inject
    public StaffHandler(ServerBuilder serverBuilder, SessionFactory sessionFactory) {
        super(serverBuilder);
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void setupServer(Javalin server) {
        server.get("/staff/role/list/<role>", new AccountListHandler().handler(), JwtHandler.Role.STAFF);
        server.put("/staff/profile/update/<id>", new ProfileUpdateHandler().handler(), JwtHandler.Role.STAFF);
        server.put("/staff/student/update/<id>", new StudentUpdateHandler().handler(), JwtHandler.Role.STAFF);

        server.get("/staff/class/list", new ClassListHandler().handler(), JwtHandler.Role.STAFF);
        server.put("/staff/class/update/<id>", new ClassUpdateHandler().handler(), JwtHandler.Role.STAFF);
        server.post("/staff/class/create", new ClassCreateHandler().handler(), JwtHandler.Role.STAFF);
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
                        list.add(AccountWithStudentProfileOutput.fromEntity(account, id -> Optional.ofNullable(session.get(Profile.class, account.getId())).orElseGet(Profile::new)));
                    }
                    ctx.json(new AccountWithStudentProfileListResponse(0, "Get account list", list));
                } else {
                    var query = session.createNamedQuery("Account.findByRole", Account.class).setParameter("role", role.name());
                    var accounts = query.getResultList();
                    List<AccountWithProfileOutput> list = new ArrayList<>();
                    for (var account : accounts) {
                        list.add(AccountWithProfileOutput.fromEntity(account, id -> Optional.ofNullable(session.get(Profile.class, account.getId())).orElseGet(Profile::new)));
                    }
                    ctx.json(new AccountWithProfileListResponse(0, "Get account list", list));
                }
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

                JwtHandler.Role role = JwtHandler.Role.getRole(account.getRole());
                if (role == JwtHandler.Role.ADMIN) {
                    ctx.status(403);
                    ctx.json(new Response(2, "You cannot update an admin account"));
                    return;
                } else if (role == JwtHandler.Role.STAFF) {
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

    private class StudentUpdateHandler implements ContextHandler {
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
                    .result("200", Response.class, builder -> builder.description("The class has been updated"))
                    .result("404", ClassroomResponse.class, builder -> builder.description("The homeroom teacher does not exist"))
                    .result("403", ClassroomResponse.class, builder -> builder.description("The homeroom teacher is not a teacher"));
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

                Account homeroomTeacher = session.get(Account.class, input.homeroomTeacherId());
                if (homeroomTeacher == null) {
                    ctx.status(404);
                    ctx.json(new ClassroomResponse(2, "Homeroom teacher not found", null));
                    return;
                }
                if (JwtHandler.Role.getRole(homeroomTeacher.getRole()) != JwtHandler.Role.TEACHER) {
                    ctx.status(403);
                    ctx.json(new ClassroomResponse(3, "Homeroom teacher is not a teacher", null));
                    return;
                }

                Transaction transaction = session.beginTransaction();
                classroom.setName(input.name());
                classroom.setGrade(input.grade());
                classroom.setHomeroomTeacher(homeroomTeacher);
                session.update(classroom);
                transaction.commit();

                ctx.json(new Response(0, "Class updated"));
            }
        }
    }

    private class ClassCreateHandler implements ContextHandler {
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
                    .result("404", ClassroomResponse.class, builder -> builder.description("The homeroom teacher does not exist"))
                    .result("403", ClassroomResponse.class, builder -> builder.description("The homeroom teacher is not a teacher"));
        }

        @Override
        public void handle(Context ctx) {
            ClassCreate input = ctx.bodyValidator(ClassCreate.class)
                    .check(ClassCreate::validate, "Invalid data")
                    .get();
            try (var session = sessionFactory.openSession()) {
                Account homeroomTeacher = session.get(Account.class, input.homeroomTeacherId());
                if (homeroomTeacher == null) {
                    ctx.status(404);
                    ctx.json(new ClassroomResponse(2, "Homeroom teacher not found", null));
                    return;
                }
                if (JwtHandler.Role.getRole(homeroomTeacher.getRole()) != JwtHandler.Role.TEACHER) {
                    ctx.status(403);
                    ctx.json(new ClassroomResponse(3, "Homeroom teacher is not a teacher", null));
                    return;
                }

                Transaction transaction = session.beginTransaction();
                var classroom = new Classroom();
                classroom.setName(input.name());
                classroom.setGrade(input.grade());
                classroom.setHomeroomTeacher(homeroomTeacher);
                session.save(classroom);
                transaction.commit();
                var output = ClassroomOutput.fromEntity(classroom, id -> Optional.ofNullable(session.get(Profile.class, id)).orElseGet(Profile::new));
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
                    list.add(ClassroomOutput.fromEntity(classroom, id -> Optional.ofNullable(session.get(Profile.class, id)).orElseGet(Profile::new)));
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
