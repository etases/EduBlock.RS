package io.github.etases.edublock.rs.model.input;

public record PendingRecordEntryVerify (
        long id
){
    public boolean validate() {

        return id > 0;
    }
}