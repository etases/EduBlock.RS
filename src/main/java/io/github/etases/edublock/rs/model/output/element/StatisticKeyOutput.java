package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.StatisticKey;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticKeyOutput {
    private String id;
    private int grade;
    private int year;

    public static StatisticKeyOutput fromEntity(StatisticKey statisticKey) {
        return new StatisticKeyOutput(statisticKey.getId(), statisticKey.getGrade(), statisticKey.getYear());
    }
}
