package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.model.fabric.RecordHistory;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecordHistoryOutput {
    Date timestamp;
    List<RecordOutput> record;
    String updatedBy;

    public static RecordHistoryOutput fromFabricModel(RecordHistory recordHistory) {
        return new RecordHistoryOutput(
                recordHistory.getTimestamp(),
                RecordOutput.fromFabricModel(recordHistory.getRecord()),
                recordHistory.getUpdatedBy()
        );
    }
}
