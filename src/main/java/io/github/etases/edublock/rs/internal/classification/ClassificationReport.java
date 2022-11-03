package io.github.etases.edublock.rs.internal.classification;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassificationReport {
    Classification firstHalfClassify;
    Classification secondHalfClassify;
    Classification finalClassify;
}
