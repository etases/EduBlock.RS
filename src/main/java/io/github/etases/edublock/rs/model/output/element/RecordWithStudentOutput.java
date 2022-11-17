package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.Record;
import io.github.etases.edublock.rs.internal.classification.ClassificationManager;
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
public class RecordWithStudentOutput {
    ClassroomOutput classroom = new ClassroomOutput();
    List<RecordEntryOutput> entries = Collections.emptyList();
    ClassificationReportOutput classification = new ClassificationReportOutput();
    AccountWithStudentProfileOutput student = new AccountWithStudentProfileOutput();

    public static RecordWithStudentOutput fromEntity(Record record, LongFunction<Profile> profileFunction, boolean filterUpdated, boolean fillAllSubjects) {
        if (record == null) {
            return new RecordWithStudentOutput();
        }
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

        return new RecordWithStudentOutput(
                ClassroomOutput.fromEntity(record.getClassroom(), profileFunction),
                recordEntryOutputs,
                new ClassificationReportOutput(),
                AccountWithStudentProfileOutput.fromEntity(record.getStudent(), profileFunction)
        );
    }

    public void updateClassification() {
        classification = ClassificationManager.createReport(entries);
    }
}
