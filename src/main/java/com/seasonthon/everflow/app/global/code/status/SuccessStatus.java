package com.seasonthon.everflow.app.global.code.status;


import com.seasonthon.everflow.app.global.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseErrorCode {

    // 200 OK
    OK(HttpStatus.OK, "COMMON200", "요청이 처리되었습니다."),
    // 201 Created
    CREATED(HttpStatus.CREATED, "COMMON201", "생성 완료"),
    // 202 Accepted
    ACCEPTED(HttpStatus.ACCEPTED, "COMMON202", "요청이 접수되었습니다."),
    // 204 No Content
    NO_CONTENT(HttpStatus.NO_CONTENT, "COMMON204", "처리 완료: 반환할 데이터가 없습니다."),
    // 206 Partial Content
    PARTIAL_CONTENT(HttpStatus.PARTIAL_CONTENT, "COMMON206", "부분 응답이 성공적으로 처리되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override public boolean isSuccess() { return true; }
}
