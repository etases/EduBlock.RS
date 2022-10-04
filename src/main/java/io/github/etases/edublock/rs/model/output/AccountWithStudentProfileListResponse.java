package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.AccountWithStudentProfileOutput;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
public class AccountWithStudentProfileListResponse {
    int status;
    String message;
    @Nullable
    List<AccountWithStudentProfileOutput> data;
}
