package io.github.etases.edublock.rs.model.input;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeacherWithSubjectInput {
    long teacherId;
    long subjectId;

    public boolean validate() {
        return teacherId > 0 && subjectId > 0;
    }
}
