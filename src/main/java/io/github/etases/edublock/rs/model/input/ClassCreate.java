package io.github.etases.edublock.rs.model.input;

import lombok.Value;

@Value
public class ClassCreate {
    String name;
    int grade;
    long homeroomTeacherId;

    public boolean validate() {
        return name != null && !name.isBlank()
                && grade > 0 && grade < 13
                && homeroomTeacherId > 0;
    }
}
