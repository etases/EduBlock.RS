package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import org.jetbrains.annotations.Nullable;

public class ClassroomResponse extends ResponseWithData<ClassroomOutput> {
    public ClassroomResponse(int status, String message, @Nullable ClassroomOutput data) {
        super(status, message, data);
    }
}
