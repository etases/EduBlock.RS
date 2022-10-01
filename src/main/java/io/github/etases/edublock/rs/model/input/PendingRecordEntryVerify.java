package io.github.etases.edublock.rs.model.input;

public record PendingRecordEntryVerify (
        long id,
        boolean verifyValue
){
    public boolean validate() {

        return id >0 ;
    }
}
