package io.github.etases.edublock.rs.model.output.element;

public record RecordEntryOutput(
        long subjectId,
        String subject,
        float firstHalfScore,
        float secondHalfScore,
        float finalScore
) {
}
