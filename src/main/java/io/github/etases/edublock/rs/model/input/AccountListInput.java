package io.github.etases.edublock.rs.model.input;

import lombok.Value;

import java.util.List;

@Value
public class AccountListInput {
    List<Long> accounts;

    public boolean validate() {
        return accounts != null && !accounts.isEmpty() && accounts.stream().allMatch(account -> account != null && account > 0);
    }
}
