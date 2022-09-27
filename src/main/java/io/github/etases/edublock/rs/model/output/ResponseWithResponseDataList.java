package io.github.etases.edublock.rs.model.output;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResponseWithResponseDataList<T> extends ResponseWithData<List<ResponseWithData<T>>> {
    public ResponseWithResponseDataList(int status, String message, @Nullable List<ResponseWithData<T>> data) {
        super(status, message, data);
    }
}
