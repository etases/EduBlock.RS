package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.AccountWithProfileOutput;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Value
public class AccountWithProfileResponse {
    int status;
    String message;
    @Nullable
    AccountWithProfileOutput data;
}
