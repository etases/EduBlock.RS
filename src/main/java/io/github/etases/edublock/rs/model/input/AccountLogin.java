package io.github.etases.edublock.rs.model.input;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountLogin {
    String username;
    String password;

    public boolean validate() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}
