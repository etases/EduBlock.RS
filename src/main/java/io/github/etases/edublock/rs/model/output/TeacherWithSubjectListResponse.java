package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.TeacherWithSubjectOutput;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeacherWithSubjectListResponse {
    int status;
    String message;
    @Nullable
    List<TeacherWithSubjectOutput> data;
}
