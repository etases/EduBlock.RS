package io.github.etases.edublock.rs.model.fabric;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Record {
    Map<Integer, ClassRecord> classRecords; // key : record id (class id)
}
