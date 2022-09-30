package io.github.etases.edublock.rs.model.input;

import java.util.List;

public record AccountLoginListInput(List<AccountLogin> accounts) {
    public boolean validate() {
        return accounts != null && accounts.stream().allMatch(AccountLogin::validate);
    }
}
