package io.github.etases.edublock.rs.model.input;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record ClassCreate(
        String name,
        int grade
) {
    public boolean validate() {
        return name != null && !name.isBlank()
                && grade > 0 && grade < 13;
    }

    @JsonIgnore
    public String getClassName() {
        return name;
    }
}
