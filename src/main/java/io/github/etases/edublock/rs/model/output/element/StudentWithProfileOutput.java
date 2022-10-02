package io.github.etases.edublock.rs.model.output.element;

public record StudentWithProfileOutput(
        StudentOutput student,
        ProfileOutput profile
) {
}
