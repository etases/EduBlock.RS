package io.github.etases.edublock.rs.model.output;

import io.javalin.validation.ValidationError;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class ValidationExceptionResponse {
    int code;
    String message;
    Map<String, List<ValidationError<Object>>> data;
}
