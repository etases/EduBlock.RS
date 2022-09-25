package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.input.ClassCreate;
import org.jetbrains.annotations.Nullable;

public class ClassCreateErrorResponse extends ResponseWithData<ClassCreate> {
    public ClassCreateErrorResponse(int status, String message, @Nullable ClassCreate data) {
        super(status, message, data);
    }

}
