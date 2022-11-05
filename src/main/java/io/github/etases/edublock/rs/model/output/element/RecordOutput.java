package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.Record;
import io.github.etases.edublock.rs.internal.classification.ClassificationManager;
import io.github.etases.edublock.rs.model.fabric.ClassRecord;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongFunction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecordOutput {
    ClassroomOutput classroom = new ClassroomOutput();
    List<RecordEntryOutput> entries = Collections.emptyList();
    ClassificationReportOutput classification = new ClassificationReportOutput();

    public static RecordOutput fromEntity(Record record, LongFunction<Profile> profileFunction, boolean filterUpdated, boolean fillAllSubjects) {
        var recordEntryOutputs = record.getRecordEntry().stream()
                .filter(entry -> !filterUpdated || !entry.isUpdateComplete())
                .map(entry -> RecordEntryOutput.fromEntity(entry, profileFunction)).toList();

        if (fillAllSubjects) {
            var subjects = recordEntryOutputs.stream().map(RecordEntryOutput::getSubjectId).toList();
            var remainingRecordEntryOutputs = record.getClassroom().getTeachers().stream()
                    .filter(classTeacher -> !subjects.contains(classTeacher.getSubjectId()))
                    .map(classTeacher -> {
                        var recordEntryOutput = new RecordEntryOutput();
                        recordEntryOutput.setSubjectId(classTeacher.getSubjectId());
                        recordEntryOutput.setSubject(SubjectOutput.fromInternal(classTeacher.getSubjectId()));
                        recordEntryOutput.setTeacher(AccountWithProfileOutput.fromEntity(classTeacher.getTeacher(), profileFunction));
                        return recordEntryOutput;
                    })
                    .toList();
            recordEntryOutputs = new ArrayList<>(recordEntryOutputs);
            recordEntryOutputs.addAll(remainingRecordEntryOutputs);
        }

        return new RecordOutput(
                ClassroomOutput.fromEntity(record.getClassroom(), profileFunction),
                recordEntryOutputs,
                new ClassificationReportOutput()
        );
    }

    public static RecordOutput fromFabricModel(long classId, ClassRecord classRecord) {
        var classroom = ClassroomOutput.fromFabricModel(classId, classRecord);

        List<RecordEntryOutput> recordEntryOutputs = new ArrayList<>();
        for (var subjectEntry : classRecord.getSubjects().entrySet()) {
            var subject = subjectEntry.getValue();
            var recordEntry = RecordEntryOutput.fromFabricModel(subjectEntry.getKey(), subject);
            recordEntryOutputs.add(recordEntry);
        }

        return new RecordOutput(classroom, recordEntryOutputs, ClassificationReportOutput.fromFabricModel(classRecord.getClassification()));
    }

    public static List<RecordOutput> fromFabricModel(io.github.etases.edublock.rs.model.fabric.Record record) {
        List<RecordOutput> recordOutputs = new ArrayList<>();
        record.getClassRecords().forEach((key, value) -> recordOutputs.add(fromFabricModel(key, value)));
        return recordOutputs;
    }

    public void updateClassification() {
        classification = ClassificationManager.createReport(entries);
    }
}
