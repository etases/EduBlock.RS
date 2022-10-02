package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.AccountWithStudentProfileOutput;
import org.jetbrains.annotations.Nullable;

public class AccountWithStudentProfileResponse extends ResponseWithData<AccountWithStudentProfileOutput> {
    public AccountWithStudentProfileResponse(int status, String message, @Nullable AccountWithStudentProfileOutput data) {
        super(status, message, data);
    }
}
