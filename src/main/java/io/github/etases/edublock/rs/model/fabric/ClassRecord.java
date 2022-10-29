package io.github.etases.edublock.rs.model.fabric;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ClassRecord {
    int year;
    int grade;
    String className;
    Map<Long, Subject> subjects; // key : subject id
    Classification classification;

    public static ClassRecord clone(ClassRecord classRecord) {
        if (classRecord == null) {
            var clone = new ClassRecord();
            clone.setSubjects(new HashMap<>());
            clone.setClassification(new Classification());
            return clone;
        }
        Classification cloneClassification = Classification.clone(classRecord.getClassification());
        var cloneSubjects = new HashMap<Long, Subject>();
        if (classRecord.getSubjects() != null) {
            for (var entry : classRecord.getSubjects().entrySet()) {
                cloneSubjects.put(entry.getKey(), Subject.clone(entry.getValue()));
            }
        }
        return new ClassRecord(classRecord.getYear(), classRecord.getGrade(), classRecord.getClassName(), cloneSubjects, cloneClassification);
    }
}