package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.input.AccountLogin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AccountLoginErrorListResponse extends ResponseWithResponseDataList<AccountLogin> {
    public AccountLoginErrorListResponse(int status, String message, @Nullable List<ResponseWithData<AccountLogin>> responseWithData) {
        super(status, message, responseWithData);
    }
}
