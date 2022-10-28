package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.SubjectOutput;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubjectResponse {
    int status;
    String message;
    @Nullable
    SubjectOutput data;
}