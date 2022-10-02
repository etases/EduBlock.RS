package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.PendingRecordEntry;
import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.RecordEntry;

import java.util.function.LongFunction;

public record RecordEntryOutput(
        long subjectId,
        SubjectOutput subject,
        float firstHalfScore,
        float secondHalfScore,
        float finalScore,
        AccountWithProfileOutput teacher,
        AccountWithProfileOutput requester
) {
    public static RecordEntryOutput fromEntity(RecordEntry recordEntry, LongFunction<Profile> profileFunction) {
        return new RecordEntryOutput(
                recordEntry.getSubject().getId(),
                SubjectOutput.fromEntity(recordEntry.getSubject()),
                recordEntry.getFirstHalfScore(),
                recordEntry.getSecondHalfScore(),
                recordEntry.getFinalScore(),
                AccountWithProfileOutput.fromEntity(recordEntry.getTeacher(), profileFunction),
                AccountWithProfileOutput.fromEntity(recordEntry.getRequester(), profileFunction)
        );
    }

    public static RecordEntryOutput fromEntity(PendingRecordEntry recordEntry, LongFunction<Profile> profileFunction) {
        return new RecordEntryOutput(
                recordEntry.getSubject().getId(),
                SubjectOutput.fromEntity(recordEntry.getSubject()),
                recordEntry.getFirstHalfScore(),
                recordEntry.getSecondHalfScore(),
                recordEntry.getFinalScore(),
                AccountWithProfileOutput.fromEntity(recordEntry.getTeacher(), profileFunction),
                AccountWithProfileOutput.fromEntity(recordEntry.getRequester(), profileFunction)
        );
    }
}
