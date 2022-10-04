package io.github.etases.edublock.rs.model.input;

import lombok.Value;

import java.util.List;

@Value
public class AccountCreateListInput {
    List<AccountCreate> accounts;

    public boolean validate() {
        return accounts != null && accounts.stream().allMatch(AccountCreate::validate);
    }
}
