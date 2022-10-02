package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.AccountWithProfileOutput;
import org.jetbrains.annotations.Nullable;

public class AccountWithProfileResponse extends ResponseWithData<AccountWithProfileOutput> {
    public AccountWithProfileResponse(int status, String message, @Nullable AccountWithProfileOutput data) {
        super(status, message, data);
    }
}
