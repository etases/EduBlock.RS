package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.internal.classification.Classification;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassificationOutput {
    String identifier = "";
    String name = "";
    List<String> otherNames = Collections.emptyList();
    int level = Integer.MAX_VALUE;

    public static ClassificationOutput fromInternal(Classification classification) {
        return new ClassificationOutput(
                classification.getIdentifier(),
                classification.getName(),
                classification.getOtherNames(),
                classification.getLevel()
        );
    }
}
