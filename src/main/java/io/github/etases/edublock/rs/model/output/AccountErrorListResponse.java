package io.github.etases.edublock.rs.model.output;

import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
public class AccountErrorListResponse {
    int status;
    String message;
    @Nullable
    List<ErrorData> data;

    @Value
    public static class ErrorData {
        int status;
        String message;
        long data;
    }
}
