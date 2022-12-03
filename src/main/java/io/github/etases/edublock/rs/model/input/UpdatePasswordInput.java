package io.github.etases.edublock.rs.model.input;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePasswordInput {
    String oldPassword;
    String newPassword;

    public boolean validate() {
        return oldPassword != null && !oldPassword.isEmpty() && newPassword != null && !newPassword.isEmpty();
    }
}
