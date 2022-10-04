package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Student;
import lombok.Value;

@Value
public class StudentOutput {
    long id;
    String ethnic;
    String fatherName;
    String fatherJob;
    String motherName;
    String motherJob;
    String guardianName;
    String guardianJob;
    String homeTown;

    public static StudentOutput fromEntity(Student student) {
        return new StudentOutput(
                student.getId(),
                student.getEthnic(),
                student.getFatherName(),
                student.getFatherJob(),
                student.getMotherName(),
                student.getMotherJob(),
                student.getGuardianName(),
                student.getGuardianJob(),
                student.getHomeTown()
        );
    }
}

