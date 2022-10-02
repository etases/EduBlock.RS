package io.github.etases.edublock.rs.model.input;

import java.util.List;

public record TeacherWithSubjectListInput(List<TeacherWithSubjectInput> teachers) {
    public boolean validate() {
        return teachers != null && teachers.stream().allMatch(TeacherWithSubjectInput::validate);
    }
}
