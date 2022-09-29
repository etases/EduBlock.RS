package io.github.etases.edublock.rs.model.output;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PendingRecordEntryListResponse extends ResponseWithData<PendingRecordEntryOutPut>{
    public PendingRecordEntryListResponse(int status, String message, @Nullable PendingRecordEntryOutPut data) {
        super(status, message, data);
    }
}
