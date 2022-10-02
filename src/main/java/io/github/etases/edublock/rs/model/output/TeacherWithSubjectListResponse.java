package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.TeacherWithSubjectOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeacherWithSubjectListResponse extends ResponseWithData<List<TeacherWithSubjectOutput>> {
    public TeacherWithSubjectListResponse(int status, String message, @Nullable List<TeacherWithSubjectOutput> data) {
        super(status, message, data);
    }
}
