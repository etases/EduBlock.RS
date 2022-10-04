package io.github.etases.edublock.rs.model.output;

import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Value
public class StringResponse {
    int status;
    String message;
    @Nullable
    String data;
}
