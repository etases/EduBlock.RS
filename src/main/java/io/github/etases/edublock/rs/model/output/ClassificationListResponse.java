package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.ClassificationOutput;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassificationListResponse {
    int status;
    String message;
    @Nullable
    List<ClassificationOutput> data;
}
