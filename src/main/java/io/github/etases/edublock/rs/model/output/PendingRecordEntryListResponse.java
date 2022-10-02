package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.PendingRecordEntryOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PendingRecordEntryListResponse extends ResponseWithData<List<PendingRecordEntryOutput>> {
    public PendingRecordEntryListResponse(int status, String message, @Nullable List<PendingRecordEntryOutput> data) {
        super(status, message, data);
    }
}
