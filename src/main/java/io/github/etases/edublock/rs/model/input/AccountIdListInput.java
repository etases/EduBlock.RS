package io.github.etases.edublock.rs.model.input;

import java.util.List;

public record AccountIdListInput(List<Long> ids) {
    public boolean validate() {
        return ids != null && !ids.isEmpty();
    }
}
