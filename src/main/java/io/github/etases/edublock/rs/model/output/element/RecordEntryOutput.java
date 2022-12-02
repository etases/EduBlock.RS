package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.RecordEntry;
import io.github.etases.edublock.rs.model.fabric.Subject;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
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

    float firstHalfOral1 = 0;
    float firstHalfOral2 = 0;
    float firstHalfOral3 = 0;
    float firstHalfMinute1 = 0;
    float firstHalfMinute2 = 0;
    float firstHalfMinute3 = 0;
    float firstHalfSession1 = 0;
    float firstHalfSession2 = 0;
    float firstHalfSession3 = 0;
    float firstHalfSession4 = 0;
    float firstHalfSession5 = 0;
    float firstHalfSession6 = 0;
    float firstHalfFinal = 0;
    float firstHalfScore = 0;

    float secondHalfOral1 = 0;
    float secondHalfOral2 = 0;
    float secondHalfOral3 = 0;
    float secondHalfMinute1 = 0;
    float secondHalfMinute2 = 0;
    float secondHalfMinute3 = 0;
    float secondHalfSession1 = 0;
    float secondHalfSession2 = 0;
    float secondHalfSession3 = 0;
    float secondHalfSession4 = 0;
    float secondHalfSession5 = 0;
    float secondHalfSession6 = 0;
    float secondHalfFinal = 0;
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
        if (recordEntry == null) {
            return new RecordEntryOutput();
        }
        return new RecordEntryOutput(
                recordEntry.getSubjectId(),
                SubjectOutput.fromInternal(recordEntry.getSubjectId()),

                recordEntry.getFirstHalfOral1(),
                recordEntry.getFirstHalfOral2(),
                recordEntry.getFirstHalfOral3(),
                recordEntry.getFirstHalfMinute1(),
                recordEntry.getFirstHalfMinute2(),
                recordEntry.getFirstHalfMinute3(),
                recordEntry.getFirstHalfSession1(),
                recordEntry.getFirstHalfSession2(),
                recordEntry.getFirstHalfSession3(),
                recordEntry.getFirstHalfSession4(),
                recordEntry.getFirstHalfSession5(),
                recordEntry.getFirstHalfSession6(),
                recordEntry.getFirstHalfFinal(),
                recordEntry.getFirstHalfScore(),

                recordEntry.getSecondHalfOral1(),
                recordEntry.getSecondHalfOral2(),
                recordEntry.getSecondHalfOral3(),
                recordEntry.getSecondHalfMinute1(),
                recordEntry.getSecondHalfMinute2(),
                recordEntry.getSecondHalfMinute3(),
                recordEntry.getSecondHalfSession1(),
                recordEntry.getSecondHalfSession2(),
                recordEntry.getSecondHalfSession3(),
                recordEntry.getSecondHalfSession4(),
                recordEntry.getSecondHalfSession5(),
                recordEntry.getSecondHalfSession6(),
                recordEntry.getSecondHalfFinal(),
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
