package io.github.etases.edublock.rs.model.output.element;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaginationInfo {
    int totalPages;
    int totalEntries;
    int pageNumber;
    int pageSize;
    boolean hasPreviousPage;
    boolean hasNextPage;
}
