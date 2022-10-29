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
    float firstHalfScore;
    float secondHalfScore;
    float finalScore;

    public static Subject clone(Subject subject) {
        return subject == null ? new Subject() : new Subject(subject.getName(), subject.getFirstHalfScore(), subject.getSecondHalfScore(), subject.getFinalScore());
    }
}
