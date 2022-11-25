package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.input.PendingRecordEntryInput;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PendingRecordEntryErrorListResponse {
    int status;
    String message;
    @Nullable
    List<PendingRecordEntryErrorListResponse.ErrorData> data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ErrorData {
        int status;
        String message;
        PendingRecordEntryInput data;
    }
}
