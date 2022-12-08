package io.github.etases.edublock.rs.model.output;

import io.github.etases.edublock.rs.model.output.element.StatisticKeyOutput;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticKeyListResponse {
    int status;
    String message;
    @Nullable
    List<StatisticKeyOutput> data;
}
