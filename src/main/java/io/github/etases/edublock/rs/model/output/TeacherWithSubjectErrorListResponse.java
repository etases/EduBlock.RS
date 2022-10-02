package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.input.TeacherWithSubjectInput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeacherWithSubjectErrorListResponse extends ResponseWithResponseDataList<TeacherWithSubjectInput>{
    public TeacherWithSubjectErrorListResponse(int status, String message, @Nullable List<ResponseWithData<TeacherWithSubjectInput>> responseWithData) {
        super(status, message, responseWithData);
    }
}
