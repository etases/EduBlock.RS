package io.github.etases.edublock.rs.model.output;

public record RecordEntryOutput(
        String classroomName,
        String subject,
        float firstHalfScore,
        float secondHalfScore,
        float finalScore
) {
}
