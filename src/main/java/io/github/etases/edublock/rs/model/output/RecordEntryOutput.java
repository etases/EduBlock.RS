package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.entity.Classroom;
import io.github.etases.edublock.rs.entity.Subject;

public record RecordEntryOutput (
        String classroomName,
        String subject,
        float firstHalfScore,
        float secondHalfScore,
        float finalScore
){
}
