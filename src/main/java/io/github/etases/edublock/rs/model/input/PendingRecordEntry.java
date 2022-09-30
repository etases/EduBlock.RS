package io.github.etases.edublock.rs.model.input;

import io.github.etases.edublock.rs.entity.Account;

public record PendingRecordEntry(float firstHalfScore, float secondHalfScore, float finalScore, Account teacher, RecordRequest record) {

    public boolean validate(){
        boolean isFirstHalfScoreValid = 10 >= firstHalfScore && firstHalfScore >= 0;
        boolean isSecondHalfScore = 10 >= secondHalfScore && secondHalfScore >= 0;
        boolean isFinalScoreValid = 10 >= finalScore && finalScore >= 0;
        boolean isTeacherValid = !teacher.equals(null);
        boolean isRecordValid = !record.equals(null);

        return isFirstHalfScoreValid && isSecondHalfScore && isFinalScoreValid && isTeacherValid && isRecordValid;
    }
}
