package com.seasonthon.everflow.app.global.code.dto;

import com.seasonthon.everflow.app.global.code.BaseErrorCode;
import com.seasonthon.everflow.app.global.code.status.SuccessStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> of(BaseErrorCode rc, T data) {
        return rc.toResponse(data);
    }

    public static <T> ApiResponse<T> onSuccess(T result){
        return new ApiResponse<>(true, SuccessStatus.OK.getCode() , SuccessStatus.OK.getMessage(), result);
    }

    public static <T> ApiResponse<T> onFailure(String code, String message, T data) {
        return new ApiResponse<> (false, code, message, data);
    }
}
