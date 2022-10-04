package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.AccountWithStudentProfileOutput;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Value
public class AccountWithStudentProfileResponse {
    int status;
    String message;
    @Nullable
    AccountWithStudentProfileOutput data;
}
