package com.seasonthon.everflow.app.global.code.status;

import com.seasonthon.everflow.app.global.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    // 400 Bad Request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALID4001", "입력값 유효성 검증에 실패했습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "VALID4002", "필수 파라미터가 누락되었습니다."),
    FILE_IS_EMPTY(HttpStatus.BAD_REQUEST, "FILE4006", "업로드할 파일이 없습니다."),
    FAMILY_JOIN_FAILED(HttpStatus.BAD_REQUEST, "FAMILY40012", "가족 가입에 3회 이상 실패했습니다."),
    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "FILE40018", "지원하지 않는 이미지 형식입니다. (jpg, png만 지원)"),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE40019", "이미지 크기가 너무 큽니다. (최대 5MB)"),
    INVALID_VERIFICATION_ANSWER(HttpStatus.BAD_REQUEST, "VERIFICATION40020", "가족 검증 질문 또는 답변이 틀렸습니다."),
    SELF_APPOINTMENT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "APPOINTMENT4001", "자기 자신에게 약속을 제안할 수 없습니다."),
    // == 파라미터 관련 에러 ==
    INVALID_MONTH_PARAMETER(HttpStatus.BAD_REQUEST, "PARAMETER4001", "월(month)은 1에서 12 사이의 값이어야 합니다."),
    INVALID_DAY_PARAMETER(HttpStatus.BAD_REQUEST, "PARAMETER4002", "유효하지 않은 일(day)입니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH401", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4012", "토큰이 유효하지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4013", "토큰이 만료되었습니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "접근 권한이 없습니다."),
    APPOINTMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "APPOINTMENT4031", "약속을 삭제할 권한이 없습니다."),
    FAMILY_JOIN_ATTEMPT_EXCEEDED(HttpStatus.FORBIDDEN, "FAMILYJOIN4035","가족 가입에 4회 연속 실패했습니다. 가입 요청을 확인해주세요."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4041", "사용자를 찾을 수 없습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4044", "해당 알림을 찾을 수 없습니다."),
    FAMILY_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPANT_REWARD40414", "가족 정보를 찾을 수 없습니다."),
    NOT_IN_FAMILY_YET(HttpStatus.NOT_FOUND, "PARTICIPANT_REWARD40415", "소속된 가족 정보를 찾을 수 없습니다."),
    // == Auth 관련 에러 ==
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH401", "인증 정보가 필요합니다."),
    // == Appointment 관련 에러 ==
    APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "APPOINTMENT4041", "해당 약속을 찾을 수 없습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPANT4041", "해당 약속의 참여자가 아닙니다."),
    // == Topic 관련 에러 ==
    TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "TOPIC4041", "해당 토픽을 찾을 수 없습니다."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "ANSWER4041", "해당 답변을 찾을 수 없습니다."),
    TOPIC_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "TOPIC4001", "활성화된 토픽이 아닙니다."),
    ANSWER_ALREADY_EXISTS(HttpStatus.CONFLICT, "ANSWER4091", "이미 답변이 존재합니다. 수정 API를 이용하세요."),
    // == Home 관련 에러 ==
    HOME_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "HOME4041", "최근 30일간 답변 데이터가 없습니다."),
    FAMILY_PARTICIPATION_NOT_FOUND(HttpStatus.NOT_FOUND, "HOME4042", "해당 가족의 참여 기록이 없습니다."),
    // == BookShelf 관련 에러 ==
    BOOKSHELF_INVALID_PARAMETER(HttpStatus.BAD_REQUEST,"BOOKSHELF4001", "잘못된 책장 요청입니다."),
    BOOKSHELF_FAMILY_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKSHELF4041", "가족을 찾을 수 없습니다."),
    BOOKSHELF_MEMBERS_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKSHELF4042", "가족 구성원이 존재하지 않습니다."),
    // == Gemini 관련 에러 ==
    GEMINI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "GEMINI5041", "Gemini 요청이 시간 초과되었습니다."),
    GEMINI_HTTP_ERROR(HttpStatus.BAD_GATEWAY, "GEMINI5021", "Gemini API 호출 중 HTTP 오류가 발생했습니다."),
    GEMINI_BAD_RESPONSE(HttpStatus.BAD_GATEWAY, "GEMINI5022", "Gemini 응답 형식이 올바르지 않습니다."),
    GEMINI_CALL_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "GEMINI5031", "Gemini 호출에 실패했습니다."),
    GEMINI_EMPTY_QUESTION(HttpStatus.UNPROCESSABLE_ENTITY, "GEMINI4221", "생성된 질문이 비어 있습니다."),
    // == Memo 관련 에러 ==
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMO4041", "공유 메모를 찾을 수 없습니다."),
    MEMO_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "MEMO4031", "해당 메모를 수정할 권한이 없습니다."),
    MEMO_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "MEMO4001", "메모 내용은 800자를 초과할 수 없습니다."),
    // == Custom Bookshelf 관련 에러 ==
    BOOKSHELF_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKSHELF4043", "해당 책장 질문을 찾을 수 없습니다."),
    BOOKSHELF_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKSHELF4044", "해당 책장 답변을 찾을 수 없습니다."),
    BOOKSHELF_QUESTION_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "BOOKSHELF4031", "책장 질문을 생성할 권한이 없습니다."),
    BOOKSHELF_QUESTION_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "BOOKSHELF4032", "책장 질문을 삭제할 권한이 없습니다."),
    BOOKSHELF_ANSWER_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "BOOKSHELF4033", "책장 답변을 수정할 권한이 없습니다."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILYJOIN4040", "가입 요청을 찾을 수 없습니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON405", "허용되지 않는 HTTP 메서드입니다."),

    // 409 Conflict
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "GEN4091", "이미 존재하는 리소스입니다."),
    VERSION_CONFLICT(HttpStatus.CONFLICT, "GEN4092", "리소스 버전 충돌이 발생했습니다."),
    FAMILY_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER40911", "이미 가입된 가족이 존재합니다."),

    // 429 Too Many Requests
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "COMMON429", "요청이 너무 많습니다. 잠시 후 다시 시도하세요."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "LIMIT4291", "요청 한도를 초과했습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB5003", "데이터베이스 처리 중 오류가 발생했습니다."),
    REDIS_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "REDIS5004", "Redis 서비스에 일시적인 문제가 발생했습니다."),

    // 502, 502, 504
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "COMMON502", "Bad Gateway."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "COMMON503", "서비스를 일시적으로 사용할 수 없습니다."),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "COMMON504", "연결 시간 초과.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override public boolean isSuccess() { return false; }
}
