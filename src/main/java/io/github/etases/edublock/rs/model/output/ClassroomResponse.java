package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Value
public class ClassroomResponse {
    int status;
    String message;
    @Nullable
    ClassroomOutput data;
}
