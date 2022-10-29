package io.github.etases.edublock.rs.model.fabric;

import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.Student;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Personal {
    String firstName;
    String lastName;
    boolean male;
    String avatar;
    Date birthDate;
    String address;
    String ethnic;
    String fatherName;
    String fatherJob;
    String motherName;
    String motherJob;
    String guardianName;
    String guardianJob;
    String homeTown;

    public static Personal fromEntity(Student student, Profile profile) {
        var personal = new Personal();
        personal.setFirstName(profile.getFirstName());
        personal.setLastName(profile.getLastName());
        personal.setMale(profile.isMale());
        personal.setAvatar(profile.getAvatar());
        personal.setBirthDate(profile.getBirthDate());
        personal.setAddress(profile.getAddress());
        personal.setEthnic(student.getEthnic());
        personal.setFatherName(student.getFatherName());
        personal.setFatherJob(student.getFatherJob());
        personal.setMotherName(student.getMotherName());
        personal.setMotherJob(student.getMotherJob());
        personal.setGuardianName(student.getGuardianName());
        personal.setGuardianJob(student.getGuardianJob());
        personal.setHomeTown(student.getHomeTown());
        return personal;
    }
}
