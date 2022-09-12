package io.github.etases.edublock.rs.model.output;

import lombok.Data;

import javax.annotation.Nullable;

@Data
public class ResponseWithData<T> {
    private final int status;
    private final String message;
    private final @Nullable T data;
}
