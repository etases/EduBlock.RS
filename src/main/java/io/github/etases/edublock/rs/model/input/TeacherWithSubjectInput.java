package io.github.etases.edublock.rs.model.input;

public record TeacherWithSubjectInput(long teacherId, long subjectId) {
    public boolean validate() {
        return teacherId > 0 && subjectId > 0;
    }
}
