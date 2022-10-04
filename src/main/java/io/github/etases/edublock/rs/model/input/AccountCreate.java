package io.github.etases.edublock.rs.model.input;

import io.javalin.openapi.OpenApiIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCreate {
    String firstName;
    String lastName;
    String role;

    public boolean validate() {
        return firstName != null && !firstName.isBlank()
                && lastName != null && !lastName.isBlank()
                && role != null && !role.isBlank();
    }

    @OpenApiIgnore
    public String getUsername() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < firstName.length(); i++) {
            char c = firstName.charAt(i);
            if (Character.isLetter(c)) {
                if (i == 0) {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        for (char c : lastName.toCharArray()) {
            if (Character.isLetter(c) && Character.isUpperCase(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
