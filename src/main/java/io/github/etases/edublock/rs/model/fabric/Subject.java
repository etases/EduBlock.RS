package io.github.etases.edublock.rs.model.fabric;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Subject {
    String name;
    float firstHaftScore;
    float secondHaftScore;
    float finalScore;
}
