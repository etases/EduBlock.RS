package io.github.etases.edublock.rs.model.output;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecordEntryListResponse extends ResponseWithData<List<RecordEntryOutput>>{
    public RecordEntryListResponse(int status, String message, @Nullable List<RecordEntryOutput> data) {
        super(status, message, data);
    }
}
