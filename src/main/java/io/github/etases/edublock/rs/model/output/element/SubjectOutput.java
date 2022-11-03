package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.internal.subject.Subject;
import io.github.etases.edublock.rs.internal.subject.SubjectManager;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubjectOutput {
    long id = -1;
    String identifier = "";
    String name = "";
    List<String> otherNames = Collections.emptyList();

    public static SubjectOutput fromInternal(Subject subject) {
        return new SubjectOutput(subject.getId(), subject.getIdentifier(), subject.getName(), subject.getOtherNames());
    }

    public static SubjectOutput fromInternal(long id) {
        return Optional.ofNullable(SubjectManager.getSubject(id))
                .map(SubjectOutput::fromInternal)
                .orElse(new SubjectOutput(id, "", "", Collections.emptyList()));
    }

    public static SubjectOutput fromFabricModel(long id, io.github.etases.edublock.rs.model.fabric.Subject subject) {
        return Optional.ofNullable(SubjectManager.getSubject(id))
                .map(SubjectOutput::fromInternal)
                .orElse(new SubjectOutput(id, subject.getName(), subject.getName(), Collections.emptyList()));
    }
}
