package io.github.etases.edublock.rs.model.input;

public record ClassUpdate(String name, int grade) {
    public boolean validate() {
        boolean isValidName = name != null && !name.isBlank();
        boolean isValidGrade = grade > 0 && grade < 13;

        return isValidName && isValidGrade;
    }
}