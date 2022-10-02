package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Profile;

import java.util.function.LongFunction;

public record AccountWithProfileOutput(AccountOutput account, ProfileOutput profile) {
    public static AccountWithProfileOutput fromEntity(Account account, LongFunction<Profile> profileFunction) {
        return new AccountWithProfileOutput(
                AccountOutput.fromEntity(account),
                ProfileOutput.fromEntity(profileFunction.apply(account.getId()))
        );
    }
}
