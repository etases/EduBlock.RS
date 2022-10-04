package io.github.etases.edublock.rs.model.output;

import io.javalin.validation.ValidationError;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValidationExceptionResponse {
    int code;
    String message;
    Map<String, List<ValidationError<Object>>> data;
}
