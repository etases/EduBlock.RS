package io.github.etases.edublock.rs.model.fabric;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
}