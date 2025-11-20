package dev.charles.SimpleService.errors.exception;

import dev.charles.SimpleService.errors.errorcode.CustomErrorCode;

public class NotAuthorizedException extends RestApiException {
    public NotAuthorizedException(String message) {
        super(CustomErrorCode.NOT_AUTHORIZED, message);
    }
}
