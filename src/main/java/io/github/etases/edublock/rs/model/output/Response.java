package io.github.etases.edublock.rs.model.output;

import lombok.Value;

@Value
public class Response {
    int status;
    String message;
}
