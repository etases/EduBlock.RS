package io.github.etases.edublock.rs.model.input;

public record PendingRecordEntry(float firstHalfScore, float secondHalfScore, float finalScore) {

    public boolean validate(){
        boolean isFirstHalfScoreValid = 10 >= firstHalfScore && firstHalfScore >= 0;
        boolean isSecondHalfScore = 10 >= secondHalfScore && secondHalfScore >= 0;
        boolean isFinalScoreValid = 10 >= finalScore && finalScore >= 0;

        return isFirstHalfScoreValid && isSecondHalfScore && isFinalScoreValid;
    }
}
