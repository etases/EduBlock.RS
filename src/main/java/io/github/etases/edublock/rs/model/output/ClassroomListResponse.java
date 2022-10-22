package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import io.github.etases.edublock.rs.model.output.element.PaginationInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassroomListResponse {
    int status;
    String message;
    PaginationInfo pageInfo;
    @Nullable
    List<ClassroomOutput> data;
}
