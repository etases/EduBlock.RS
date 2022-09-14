package io.github.etases.edublock.rs.model.output;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AccountListResponse extends ResponseWithData<List<AccountOutput>> {
    public AccountListResponse(int status, String message, @Nullable List<AccountOutput> data) {
        super(status, message, data);
    }
}
