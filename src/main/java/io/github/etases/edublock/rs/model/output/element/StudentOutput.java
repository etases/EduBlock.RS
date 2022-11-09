package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Student;
import io.github.etases.edublock.rs.model.fabric.Personal;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentOutput {
    long id = 0;
    String ethnic = "";
    String fatherName = "";
    String fatherJob = "";
    String motherName = "";
    String motherJob = "";
    String guardianName = "";
    String guardianJob = "";
    String homeTown = "";

    public static StudentOutput fromEntity(Student student) {
        if (student == null) {
            return new StudentOutput();
        }
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

    public static StudentOutput fromFabricModel(long id, Personal personal) {
        return new StudentOutput(
                id,
                personal.getEthnic(),
                personal.getFatherName(),
                personal.getFatherJob(),
                personal.getMotherName(),
                personal.getMotherJob(),
                personal.getGuardianName(),
                personal.getGuardianJob(),
                personal.getHomeTown()
        );
    }
}

