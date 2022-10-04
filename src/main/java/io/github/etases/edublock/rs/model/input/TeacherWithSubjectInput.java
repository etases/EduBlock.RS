package io.github.etases.edublock.rs.model.input;

import lombok.Value;

@Value
public class TeacherWithSubjectInput {
    long teacherId;
    long subjectId;

    public boolean validate() {
        return teacherId > 0 && subjectId > 0;
    }
}
