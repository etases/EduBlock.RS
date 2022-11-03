package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.RecordEntry;
import io.github.etases.edublock.rs.model.fabric.Subject;
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
public class RecordEntryOutput {
    long subjectId = 0;
    SubjectOutput subject = new SubjectOutput();
    float firstHalfScore = 0;
    float secondHalfScore = 0;
    float finalScore = 0;
    Date requestDate = Date.from(Instant.EPOCH);
    Date approvalDate = Date.from(Instant.EPOCH);
    boolean updateComplete = false;
    boolean fromFabric = false;
    AccountWithProfileOutput teacher = new AccountWithProfileOutput();
    AccountWithProfileOutput requester = new AccountWithProfileOutput();
    AccountWithProfileOutput approver = new AccountWithProfileOutput();

    public static RecordEntryOutput fromEntity(RecordEntry recordEntry, LongFunction<Profile> profileFunction) {
        return new RecordEntryOutput(
                recordEntry.getSubjectId(),
                SubjectOutput.fromInternal(recordEntry.getSubjectId()),
                recordEntry.getFirstHalfScore(),
                recordEntry.getSecondHalfScore(),
                recordEntry.getFinalScore(),
                recordEntry.getRequestDate(),
                recordEntry.getApprovalDate(),
                recordEntry.isUpdateComplete(),
                false,
                AccountWithProfileOutput.fromEntity(recordEntry.getTeacher(), profileFunction),
                AccountWithProfileOutput.fromEntity(recordEntry.getRequester(), profileFunction),
                AccountWithProfileOutput.fromEntity(recordEntry.getApprover(), profileFunction)
        );
    }

    public static RecordEntryOutput fromFabricModel(long id, Subject subject) {
        var recordEntry = new RecordEntryOutput();
        recordEntry.setSubjectId(id);
        recordEntry.setSubject(SubjectOutput.fromFabricModel(id, subject));
        recordEntry.setFirstHalfScore(subject.getFirstHalfScore());
        recordEntry.setSecondHalfScore(subject.getSecondHalfScore());
        recordEntry.setFinalScore(subject.getFinalScore());
        recordEntry.setFromFabric(true);
        return recordEntry;
    }
}
