package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.Student;
import io.github.etases.edublock.rs.model.fabric.Personal;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.function.LongFunction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountWithStudentProfileOutput {
    AccountOutput account;
    StudentOutput student;
    ProfileOutput profile;

    public static AccountWithStudentProfileOutput fromEntity(Student student, LongFunction<Profile> profileFunction) {
        return new AccountWithStudentProfileOutput(
                AccountOutput.fromEntity(student.getAccount()),
                StudentOutput.fromEntity(student),
                ProfileOutput.fromEntity(profileFunction.apply(student.getAccount().getId()))
        );
    }

    public static AccountWithStudentProfileOutput fromFabricModel(long id, Personal personal) {
        var accountOutput = new AccountOutput();
        accountOutput.setId(id);

        return new AccountWithStudentProfileOutput(
                accountOutput,
                StudentOutput.fromFabricModel(id, personal),
                ProfileOutput.fromFabricModel(id, personal)
        );
    }
}
