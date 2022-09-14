package io.github.etases.edublock.rs.model.output;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Response {
    private int status;
    private String message;

    public Response(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
