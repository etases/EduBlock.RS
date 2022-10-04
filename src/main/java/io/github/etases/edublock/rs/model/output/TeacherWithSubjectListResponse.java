package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.TeacherWithSubjectOutput;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
public class TeacherWithSubjectListResponse {
    int status;
    String message;
    @Nullable
    List<TeacherWithSubjectOutput> data;
}
