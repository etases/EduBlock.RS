package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.Record;
import io.github.etases.edublock.rs.model.fabric.ClassRecord;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecordOutput {
    ClassroomOutput classroom;
    List<RecordEntryOutput> entries;

    public static RecordOutput fromEntity(Record record, LongFunction<Profile> profileFunction) {
        return new RecordOutput(
                ClassroomOutput.fromEntity(record.getClassroom(), profileFunction),
                record.getRecordEntry().stream().map(entry -> RecordEntryOutput.fromEntity(entry, profileFunction)).toList()
        );
    }

    public static RecordOutput fromFabricModel(long classId, ClassRecord classRecord) {
        var classroom = ClassroomOutput.fromFabricModel(classId, classRecord);

        List<RecordEntryOutput> recordEntryOutputs = new ArrayList<>();
        for (var subjectEntry : classRecord.getSubjects().entrySet()) {
            var subject = subjectEntry.getValue();
            var recordEntry = new RecordEntryOutput();
            recordEntry.setSubjectId(subjectEntry.getKey());
            recordEntry.setSubject(new SubjectOutput(
                    subjectEntry.getKey(),
                    subject.getName()
            ));
            recordEntry.setFirstHalfScore(subject.getFirstHalfScore());
            recordEntry.setSecondHalfScore(subject.getSecondHalfScore());
            recordEntry.setFinalScore(subject.getFinalScore());
            recordEntryOutputs.add(recordEntry);
        }

        return new RecordOutput(classroom, recordEntryOutputs);
    }

    public static List<RecordOutput> fromFabricModel(io.github.etases.edublock.rs.model.fabric.Record record) {
        List<RecordOutput> recordOutputs = new ArrayList<>();
        record.getClassRecords().forEach((key, value) -> recordOutputs.add(fromFabricModel(key, value)));
        return recordOutputs;
    }
}
