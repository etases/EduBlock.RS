package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.AccountWithProfileOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AccountWithProfileListResponse extends ResponseWithData<List<AccountWithProfileOutput>> {
    public AccountWithProfileListResponse(int status, String message, @Nullable List<AccountWithProfileOutput> data) {
        super(status, message, data);
    }
}
