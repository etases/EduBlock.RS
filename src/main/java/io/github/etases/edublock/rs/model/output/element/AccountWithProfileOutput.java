package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Profile;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.function.LongFunction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountWithProfileOutput {
    AccountOutput account = new AccountOutput();
    ProfileOutput profile = new ProfileOutput();

    public static AccountWithProfileOutput fromEntity(Account account, LongFunction<Profile> profileFunction) {
        return new AccountWithProfileOutput(
                AccountOutput.fromEntity(account),
                ProfileOutput.fromEntity(profileFunction.apply(account.getId()))
        );
    }
}
