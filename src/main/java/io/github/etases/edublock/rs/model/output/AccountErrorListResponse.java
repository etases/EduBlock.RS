package io.github.etases.edublock.rs.model.output;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AccountErrorListResponse extends ResponseWithResponseDataList<Long> {
    public AccountErrorListResponse(int status, String message, @Nullable List<ResponseWithData<Long>> responseWithData) {
        super(status, message, responseWithData);
    }
}
