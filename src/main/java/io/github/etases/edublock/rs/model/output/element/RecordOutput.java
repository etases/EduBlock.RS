package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.entity.Profile;
import io.github.etases.edublock.rs.entity.Record;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
}
