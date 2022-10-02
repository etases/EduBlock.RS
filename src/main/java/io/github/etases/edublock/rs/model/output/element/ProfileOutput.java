package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Profile;

import java.util.Date;

public record ProfileOutput(
        long id,
        String firstName,
        String lastName,
        String avatar,
        Date birthDate,
        String address,
        String phone,
        String email
) {
    public static ProfileOutput fromEntity(Profile profile) {
        return new ProfileOutput(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getAvatar(),
                profile.getBirthDate(),
                profile.getAddress(),
                profile.getPhone(),
                profile.getEmail()
        );
    }
}
