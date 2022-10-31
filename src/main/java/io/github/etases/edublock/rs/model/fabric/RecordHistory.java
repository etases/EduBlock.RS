package io.github.etases.edublock.rs.model.fabric;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class RecordHistory {
    Date timestamp;
    Record record;
    String updatedBy;
}
