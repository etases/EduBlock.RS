package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.PendingRecordEntry;
import io.github.etases.edublock.rs.entity.Profile;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.function.LongFunction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PendingRecordEntryOutput {
    long subjectId;
    SubjectOutput subject;
    float firstHalfScore;
    float secondHalfScore;
    float finalScore;
    Date requestDate;
    AccountWithProfileOutput teacher;
    AccountWithProfileOutput requester;

    public static PendingRecordEntryOutput fromEntity(PendingRecordEntry recordEntry, LongFunction<Profile> profileFunction) {
        return new PendingRecordEntryOutput(
                recordEntry.getSubject().getId(),
                SubjectOutput.fromEntity(recordEntry.getSubject()),
                recordEntry.getFirstHalfScore(),
                recordEntry.getSecondHalfScore(),
                recordEntry.getFinalScore(),
                recordEntry.getRequestDate(),
                AccountWithProfileOutput.fromEntity(recordEntry.getTeacher(), profileFunction),
                AccountWithProfileOutput.fromEntity(recordEntry.getRequester(), profileFunction)
        );
    }
}
