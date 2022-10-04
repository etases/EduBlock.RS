package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Profile;
import lombok.Value;

import java.util.function.LongFunction;

@Value
public class AccountWithProfileOutput {
    AccountOutput account;
    ProfileOutput profile;

    public static AccountWithProfileOutput fromEntity(Account account, LongFunction<Profile> profileFunction) {
        return new AccountWithProfileOutput(
                AccountOutput.fromEntity(account),
                ProfileOutput.fromEntity(profileFunction.apply(account.getId()))
        );
    }
}
