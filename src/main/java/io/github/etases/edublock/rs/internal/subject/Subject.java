package io.github.etases.edublock.rs.internal.subject;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.common.Validate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    public static Subject fromMap(Map<String, Object> map) {
        Subject subject = new Subject();
        Optional.ofNullable(map.get("id")).map(Objects::toString).flatMap(Validate::getNumber).map(BigDecimal::longValueExact).ifPresent(subject::setId);
        Optional.ofNullable(map.get("identifier")).map(Objects::toString).ifPresent(subject::setIdentifier);
        Optional.ofNullable(map.get("name")).map(Objects::toString).ifPresent(subject::setName);
        Optional.ofNullable(map.get("otherNames")).map(CollectionUtils::createStringListFromObject).ifPresent(subject::setOtherNames);
        return subject;
    }
}
