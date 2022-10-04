package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
public class ClassroomListResponse {
    int status;
    String message;
    @Nullable
    List<ClassroomOutput> data;
}
