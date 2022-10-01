package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Subject;

public record PendingRecordEntryOutput(
        long id,
        Subject subject,
        float firstHalfScore,
        float secondHalfScore,
        float finalScore,
        Account teacher
) {
}
