package io.github.etases.edublock.rs.model.output;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecordEntryListResponse extends ResponseWithData<RecordEntryOutput>{
    public RecordEntryListResponse(int status, String message, @Nullable RecordEntryOutput data) {
        super(status, message, data);
    }
}
