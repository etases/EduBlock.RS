package io.github.etases.edublock.rs.model.input;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubjectCreateListInput {
    List<SubjectCreate> subjects;

    public boolean validate() {
        return subjects != null && subjects.stream().allMatch(SubjectCreate::validate);
    }
}
