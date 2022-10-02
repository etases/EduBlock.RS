package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.StudentWithProfileOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StudentWithProfileListResponse extends ResponseWithData<List<StudentWithProfileOutput>> {
    public StudentWithProfileListResponse(int status, String message, @Nullable List<StudentWithProfileOutput> data) {
        super(status, message, data);
    }
}
