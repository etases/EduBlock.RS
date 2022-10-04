package io.github.etases.edublock.rs.model.input;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountListInput {
    List<Long> accounts;

    public boolean validate() {
        return accounts != null && !accounts.isEmpty() && accounts.stream().allMatch(account -> account != null && account > 0);
    }
}
