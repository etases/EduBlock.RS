package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.input.AccountLogin;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
public class AccountLoginErrorListResponse {
    int status;
    String message;
    @Nullable
    List<ErrorData> data;

    @Value
    public static class ErrorData {
        int status;
        String message;
        AccountLogin data;
    }
}
