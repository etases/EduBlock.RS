package io.github.etases.edublock.rs.model.input;

public record ClassCreate(
        String name,
        int grade,
        long homeroomTeacherId
) {
    public boolean validate() {
        return name != null && !name.isBlank()
                && grade > 0 && grade < 13;
    }
}
