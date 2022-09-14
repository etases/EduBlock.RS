package io.github.etases.edublock.rs.model.output;

public record RecordEntryOutput (
        long id,
        float firstHalfScore,
        float secondHalfScore,
        float finalScore
){
}
