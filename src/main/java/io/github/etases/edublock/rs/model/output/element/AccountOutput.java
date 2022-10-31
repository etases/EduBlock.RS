package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Account;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountOutput {
    long id = 0;
    String username = "";
    String role = "";
    Date createdAt = Date.from(Instant.EPOCH);

    public static AccountOutput fromEntity(Account account) {
        return new AccountOutput(
                account.getId(),
                account.getUsername(),
                account.getRole(),
                account.getCreatedAt()
        );
    }
}
