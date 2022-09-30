package io.github.etases.edublock.rs.model.input;

import io.github.etases.edublock.rs.entity.Classroom;
import io.github.etases.edublock.rs.entity.Student;

public record RecordRequest(Classroom classRoomId, Student studentId) {
    public boolean validate(){
        boolean isClassRoomIdValid = !classRoomId.equals(null);
        boolean isStudentIdValid = !studentId.equals(null);

        return isClassRoomIdValid && isStudentIdValid;
    }
}
