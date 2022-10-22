package io.github.etases.edublock.rs.model.input;

import io.javalin.http.Context;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.hsgamer.hscore.common.Validate;

import java.math.BigDecimal;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaginationParameter {
    int pageSize;
    int pageNumber;

    public static PaginationParameter fromQuery(Context ctx) {
        int pageSize = Optional.ofNullable(ctx.queryParam("pageSize")).flatMap(Validate::getNumber).map(BigDecimal::intValue).filter(size -> size > 0).orElse(10);
        int pageNumber = Optional.ofNullable(ctx.queryParam("pageNumber")).flatMap(Validate::getNumber).map(BigDecimal::intValue).filter(page -> page > 0).orElse(1);
        return new PaginationParameter(pageSize, pageNumber);
    }
}
