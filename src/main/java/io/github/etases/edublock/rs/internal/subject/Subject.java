package io.github.etases.edublock.rs.internal.subject;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Subject implements Serializable {
    long id;
    String identifier;
    String name;
    List<String> otherNames;
}
