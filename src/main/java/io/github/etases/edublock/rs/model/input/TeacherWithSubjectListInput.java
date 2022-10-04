package io.github.etases.edublock.rs.model.input;

import lombok.Value;

import java.util.List;

@Value
public class TeacherWithSubjectListInput {
    List<TeacherWithSubjectInput> teachers;

    public boolean validate() {
        return teachers != null && teachers.stream().allMatch(TeacherWithSubjectInput::validate);
    }
}
