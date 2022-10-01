package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.RecordOutput;
import org.jetbrains.annotations.Nullable;

public class RecordResponse extends ResponseWithData<RecordOutput> {
    public RecordResponse(int status, String message, @Nullable RecordOutput data) {
        super(status, message, data);
    }
}
