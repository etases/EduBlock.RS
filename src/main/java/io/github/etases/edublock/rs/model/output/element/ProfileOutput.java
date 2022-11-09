package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.model.fabric.Personal;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileOutput {
    long id = 0;
    String firstName = "";
    String lastName = "";
    boolean male = true;
    String avatar = "";
    Date birthDate = Date.from(Instant.EPOCH);
    String address = "";
    String phone = "";
    String email = "";

    public static ProfileOutput fromEntity(Profile profile) {
        if (profile == null) {
            return new ProfileOutput();
        }
        return new ProfileOutput(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.isMale(),
                profile.getAvatar(),
                profile.getBirthDate(),
                profile.getAddress(),
                profile.getPhone(),
                profile.getEmail()
        );
    }

    public static ProfileOutput fromFabricModel(long id, Personal personal) {
        return new ProfileOutput(
                id,
                personal.getFirstName(),
                personal.getLastName(),
                personal.isMale(),
                personal.getAvatar(),
                personal.getBirthDate(),
                personal.getAddress(),
                "",
                ""
        );
    }
}
