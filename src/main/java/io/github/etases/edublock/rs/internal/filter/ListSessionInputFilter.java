package io.github.etases.edublock.rs.internal.filter;

import io.javalin.http.Context;
import me.hsgamer.hscore.collections.map.CaseInsensitiveStringHashMap;
import org.hibernate.Session;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

public class ListSessionInputFilter<T> {
    private final Map<String, Filter<T>> filters;

    private ListSessionInputFilter() {
        this.filters = new CaseInsensitiveStringHashMap<>();
    }

    public static <T> ListSessionInputFilter<T> create() {
        return new ListSessionInputFilter<>();
    }

    public ListSessionInputFilter<T> addFilter(String name, Filter<T> filter) {
        this.filters.put(name, filter);
        return this;
    }

    public ListSessionInputFilter<T> addFilter(String name, BiPredicate<String, T> predicate) {
        return addFilter(name, (session, input, t) -> predicate.test(input, t));
    }

    public List<T> filter(Session session, List<T> list, String input, String filterName) {
        Filter<T> filter;
        if (filterName.equalsIgnoreCase("all")) {
            filter = (session1, input1, t) -> filters.values().stream().anyMatch(filter1 -> filter1.test(session1, input1, t));
        } else {
            filter = this.filters.get(filterName);
        }
        if (filter == null) {
            return list;
        }
        return list.stream().filter(t -> filter.test(session, input, t)).toList();
    }

    public List<T> filter(Session session, List<T> input, Context context) {
        String filterName = Optional.ofNullable(context.queryParam("filter")).orElse("all");
        String filterInput = context.queryParam("input");
        if (filterInput == null || filterInput.isEmpty()) {
            return input;
        }
        return filter(session, input, filterInput, filterName);
    }

    public interface Filter<T> {
        boolean test(Session session, String input, T t);
    }
}
