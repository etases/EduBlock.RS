package io.github.etases.edublock.rs.model.input;

import java.util.List;

public record AccountListInput(List<Long> accounts) {
    public boolean validate() {
        return accounts != null && !accounts.isEmpty() && accounts.stream().allMatch(account -> account != null && account > 0);
    }
}
