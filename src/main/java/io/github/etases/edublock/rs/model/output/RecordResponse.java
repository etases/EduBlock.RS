package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.RecordOutput;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Value
public class RecordResponse {
    int status;
    String message;
    @Nullable
    RecordOutput data;
}
