package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.input.AccountCreate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AccountCreateErrorListResponse extends ResponseWithResponseDataList<AccountCreate> {
    public AccountCreateErrorListResponse(int status, String message, @Nullable List<ResponseWithData<AccountCreate>> responseWithData) {
        super(status, message, responseWithData);
    }
}
