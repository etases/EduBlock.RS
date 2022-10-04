package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.AccountWithProfileOutput;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
public class AccountWithProfileListResponse {
    int status;
    String message;
    @Nullable
    List<AccountWithProfileOutput> data;
}
