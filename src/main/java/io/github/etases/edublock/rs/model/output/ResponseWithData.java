package io.github.etases.edublock.rs.model.output;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
@RequiredArgsConstructor
public class ResponseWithData<T> extends Response {
    private @Nullable T data;

    public ResponseWithData(int status, String message, @Nullable T data) {
        super(status, message);
        this.data = data;
    }
}
