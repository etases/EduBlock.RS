package io.github.etases.edublock.rs.model.input;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubjectCreate {
    String name;

    public boolean validate() {
        return name != null && !name.isBlank();
    }
}
