package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.ClassTeacher;
import io.github.etases.edublock.rs.entity.Profile;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.function.LongFunction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeacherWithSubjectOutput {
    AccountWithProfileOutput account;
    SubjectOutput subject;

    public static TeacherWithSubjectOutput fromEntity(ClassTeacher classTeacher, LongFunction<Profile> profileFunction) {
        return new TeacherWithSubjectOutput(
                AccountWithProfileOutput.fromEntity(classTeacher.getTeacher(), profileFunction),
                SubjectOutput.fromEntity(classTeacher.getSubject())
        );
    }
}
