package io.github.etases.edublock.rs.model.input;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticKeyCreateInput {
    int grade;
    int year;

    public boolean validate() {
        boolean isGradeValid = grade > 0 && grade < 13;
        boolean isYearValid = year > 0;

        return isGradeValid && isYearValid;
    }
}
