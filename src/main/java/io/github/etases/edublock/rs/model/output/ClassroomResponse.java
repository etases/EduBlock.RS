package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.ClassroomOutput;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassroomResponse {
    int status;
    String message;
    @Nullable
    ClassroomOutput data;
}
