package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Record;

public record PendingRecordEntryOutPut(
        float firstHalfScore,
        float secondHalfScore,
        float finalScore,
        Account teacher,
        Record record
) {
}
