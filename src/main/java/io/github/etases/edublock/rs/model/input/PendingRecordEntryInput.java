package io.github.etases.edublock.rs.model.input;

import io.github.etases.edublock.rs.entity.Account;
import io.github.etases.edublock.rs.entity.Subject;

public record PendingRecordEntryInput(float firstHalfScore, float secondHalfScore, float finalScore, Subject subject) {

    public boolean validate(){
        boolean isFirstHalfScoreValid = 10 >= firstHalfScore && firstHalfScore >= 0;
        boolean isSecondHalfScore = 10 >= secondHalfScore && secondHalfScore >= 0;
        boolean isFinalScoreValid = 10 >= finalScore && finalScore >= 0;
        boolean isSubjectValid = !subject.equals(null);

        return isFirstHalfScoreValid && isSecondHalfScore && isFinalScoreValid && isSubjectValid;
    }
}
