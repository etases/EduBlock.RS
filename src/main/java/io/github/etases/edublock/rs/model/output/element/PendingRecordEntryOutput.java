package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.PendingRecordEntry;
import io.github.etases.edublock.rs.entity.Profile;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Date;
import java.util.function.LongFunction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PendingRecordEntryOutput {
    long id;
    long subjectId = 0;
    SubjectOutput subject = new SubjectOutput();
    float firstHalfScore = 0;
    float secondHalfScore = 0;
    float finalScore = 0;
    Date requestDate = Date.from(Instant.EPOCH);
    AccountWithProfileOutput teacher = new AccountWithProfileOutput();
    AccountWithProfileOutput requester = new AccountWithProfileOutput();
    AccountWithStudentProfileOutput student = new AccountWithStudentProfileOutput();
    ClassroomOutput classroom = new ClassroomOutput();

    public static PendingRecordEntryOutput fromEntity(PendingRecordEntry recordEntry, LongFunction<Profile> profileFunction) {
        if (recordEntry == null) {
            return new PendingRecordEntryOutput();
        }
        return new PendingRecordEntryOutput(
                recordEntry.getId(),
                recordEntry.getSubjectId(),
                SubjectOutput.fromInternal(recordEntry.getSubjectId()),
                recordEntry.getFirstHalfScore(),
                recordEntry.getSecondHalfScore(),
                recordEntry.getFinalScore(),
                recordEntry.getRequestDate(),
                AccountWithProfileOutput.fromEntity(recordEntry.getTeacher(), profileFunction),
                AccountWithProfileOutput.fromEntity(recordEntry.getRequester(), profileFunction),
                AccountWithStudentProfileOutput.fromEntity(recordEntry.getRecord().getStudent(), profileFunction),
                ClassroomOutput.fromEntity(recordEntry.getRecord().getClassroom(), profileFunction)
        );
    }
}
