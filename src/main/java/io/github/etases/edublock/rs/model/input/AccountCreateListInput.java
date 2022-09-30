package io.github.etases.edublock.rs.model.input;

import java.util.List;

public record AccountCreateListInput(List<AccountCreate> accounts) {
    public boolean validate() {
        return accounts != null && accounts.stream().allMatch(AccountCreate::validate);
    }
}
