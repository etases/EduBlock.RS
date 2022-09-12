package io.github.etases.edublock.rs.model.output;

import org.jetbrains.annotations.Nullable;

public class LoginResponse extends ResponseWithData<String> {
    public LoginResponse(int status, String message, @Nullable String data) {
        super(status, message, data);
    }
}
