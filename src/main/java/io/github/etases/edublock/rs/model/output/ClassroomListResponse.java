package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClassroomListResponse extends ResponseWithData<List<ClassroomOutput>> {
    public ClassroomListResponse(int status, String message, @Nullable List<ClassroomOutput> data) {
        super(status, message, data);
    }
}
