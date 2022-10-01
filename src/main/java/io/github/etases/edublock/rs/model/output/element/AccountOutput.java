package io.github.etases.edublock.rs.model.output.element;

import java.util.Date;

public record AccountOutput(
        long id,
        String username,
        String firstName,
        String lastName,
        String avatar,
        Date birthDate,
        String address,
        String phone,
        String email,
        String role
) {
}
