package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.model.fabric.RecordHistory;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecordHistoryOutput {
    Date timestamp = Date.from(Instant.EPOCH);
    List<RecordOutput> record = Collections.emptyList();
    String updatedBy = "";

    public static RecordHistoryOutput fromFabricModel(RecordHistory recordHistory) {
        var recordList = RecordOutput.fromFabricModel(recordHistory.getRecord());
        recordList.forEach(record -> {
            var entries = record.getEntries();
            if (entries != null) {
                entries.forEach(entry -> {
                    entry.setRequestDate(recordHistory.getTimestamp());
                    entry.setApprovalDate(recordHistory.getTimestamp());
                });
            }
        });
        return new RecordHistoryOutput(
                recordHistory.getTimestamp(),
                recordList,
                recordHistory.getUpdatedBy()
        );
    }
}
