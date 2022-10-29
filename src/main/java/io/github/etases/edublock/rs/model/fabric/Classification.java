package io.github.etases.edublock.rs.model.fabric;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Classification {
    String firstHalfClassify;
    String secondHalfClassify;
    String finalClassify;

    public static Classification clone(Classification classification) {
        return classification == null ? new Classification() : new Classification(classification.getFirstHalfClassify(), classification.getSecondHalfClassify(), classification.getFinalClassify());
    }
}
