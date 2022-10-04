package io.github.etases.edublock.rs.model.input;

import lombok.Value;

import java.util.List;

@Value
public class AccountLoginListInput {
    List<AccountLogin> accounts;

    public boolean validate() {
        return accounts != null && accounts.stream().allMatch(AccountLogin::validate);
    }
}
