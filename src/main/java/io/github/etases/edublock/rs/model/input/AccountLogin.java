package io.github.etases.edublock.rs.model.input;

import lombok.Value;

@Value
public class AccountLogin {
    String username;
    String password;

    public boolean validate() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}
