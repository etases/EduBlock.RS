package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.Student;

import java.util.function.LongFunction;

public record AccountWithStudentProfileOutput(
        AccountOutput account,
        StudentOutput student,
        ProfileOutput profile
) {
    public static AccountWithStudentProfileOutput fromEntity(Student student, LongFunction<Profile> profileFunction) {
        return new AccountWithStudentProfileOutput(
                AccountOutput.fromEntity(student.getAccount()),
                StudentOutput.fromEntity(student),
                ProfileOutput.fromEntity(profileFunction.apply(student.getAccount().getId()))
        );
    }
}
