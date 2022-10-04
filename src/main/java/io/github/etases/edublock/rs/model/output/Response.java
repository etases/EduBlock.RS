package io.github.etases.edublock.rs.model.output;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Response {
    int status;
    String message;
}
