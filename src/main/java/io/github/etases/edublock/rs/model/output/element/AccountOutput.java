package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Account;
import lombok.Value;

@Value
public class AccountOutput {
    long id;
    String username;
    String role;

    public static AccountOutput fromEntity(Account account) {
        return new AccountOutput(
                account.getId(),
                account.getUsername(),
                account.getRole()
        );
    }
}
