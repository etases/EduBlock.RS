package io.github.etases.edublock.rs.model.input;

import lombok.Value;

@Value
public class PendingRecordEntryVerify {
    long id;
    boolean accepted;

    public boolean validate() {
        return id > 0;
    }
}
