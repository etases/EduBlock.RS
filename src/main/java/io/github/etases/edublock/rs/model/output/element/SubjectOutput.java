package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Subject;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubjectOutput {
    long id;
    String name;

    public static SubjectOutput fromEntity(Subject subject) {
        return new SubjectOutput(subject.getId(), subject.getName());
    }
}
