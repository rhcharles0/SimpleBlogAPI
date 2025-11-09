package dev.charles.SimpleService.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode implements ErrorCode {

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid parameter included"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Cannot find resource."),
    DUPLICATED_RESOURCE(HttpStatus.CONFLICT, "Duplicated resource"),
    NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "Not Authorized user"),
    ;

    private final HttpStatus httpStatus;
    private final String message;

}