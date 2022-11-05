package io.github.etases.edublock.rs.internal.pagination;

import io.github.etases.edublock.rs.model.input.PaginationParameter;
import io.github.etases.edublock.rs.model.output.element.PaginationInfo;
import lombok.experimental.UtilityClass;
import me.hsgamer.hscore.common.Pair;

import java.util.List;

@UtilityClass
public final class PaginationUtil {
    public static <T> Pair<List<T>, PaginationInfo> getPagedList(List<T> list, PaginationParameter parameter) {
        int pageSize = parameter.getPageSize();
        int pageNumber = parameter.getPageNumber();
        int totalEntries = list.size();
        int totalPages = (int) Math.ceil(totalEntries / (double) pageSize);
        int fromIndex = Math.min((pageNumber - 1) * pageSize, totalEntries);
        int toIndex = Math.min(fromIndex + pageSize, totalEntries);
        boolean hasNextPage = pageNumber < totalPages;
        boolean hasPreviousPage = pageNumber > 1;
        return Pair.of(list.subList(fromIndex, toIndex), new PaginationInfo(totalPages, totalEntries, pageNumber, pageSize, hasPreviousPage, hasNextPage));
    }
}
