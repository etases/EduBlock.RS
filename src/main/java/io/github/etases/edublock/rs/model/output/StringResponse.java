package io.github.etases.edublock.rs.model.output;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StringResponse {
    int status;
    String message;
    @Nullable
    String data;
}
