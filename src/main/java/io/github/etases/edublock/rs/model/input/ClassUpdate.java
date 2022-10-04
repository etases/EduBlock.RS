package io.github.etases.edublock.rs.model.input;

import lombok.Value;

@Value
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
