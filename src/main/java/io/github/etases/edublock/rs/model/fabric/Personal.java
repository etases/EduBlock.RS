package io.github.etases.edublock.rs.model.fabric;

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
}
