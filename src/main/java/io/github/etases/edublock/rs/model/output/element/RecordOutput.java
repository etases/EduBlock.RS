package io.github.etases.edublock.rs.model.output.element;

import java.util.List;

public record RecordOutput(
        long classroomId,
        String classroomName,
        List<RecordEntryOutput> entries
) {
}
