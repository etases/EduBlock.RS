package io.github.etases.edublock.rs.model.input;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassUpdate {
    String name;
    int grade;
    long homeroomTeacherId;

    public boolean validate() {
        boolean isValidName = name != null && !name.isBlank();
        boolean isValidGrade = grade > 0 && grade < 13;

        return isValidName && isValidGrade;
    }
}
