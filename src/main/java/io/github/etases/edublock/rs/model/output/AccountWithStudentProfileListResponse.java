package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.AccountWithStudentProfileOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AccountWithStudentProfileListResponse extends ResponseWithData<List<AccountWithStudentProfileOutput>> {
    public AccountWithStudentProfileListResponse(int status, String message, @Nullable List<AccountWithStudentProfileOutput> data) {
        super(status, message, data);
    }
}
