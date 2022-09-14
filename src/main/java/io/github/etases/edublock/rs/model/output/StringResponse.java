package io.github.etases.edublock.rs.model.output;

import org.jetbrains.annotations.Nullable;

public class StringResponse extends ResponseWithData<String> {
    public StringResponse(int status, String message, @Nullable String data) {
        super(status, message, data);
    }
}
