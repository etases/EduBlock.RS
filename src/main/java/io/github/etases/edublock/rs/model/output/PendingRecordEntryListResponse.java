package io.github.etases.edublock.rs.model.output;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class PendingRecordEntryListResponse{
    private int status;
    private String message;

    public PendingRecordEntryListResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
