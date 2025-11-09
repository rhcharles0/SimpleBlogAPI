package dev.charles.SimpleService.errors.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolation;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.FieldError;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class ErrorResponse {

    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<ValidationError> errors;

    @Getter
    @Builder
    @RequiredArgsConstructor
    public static class ValidationError {

        private final String property;
        private final String message;

        public static ValidationError of(final FieldError fieldError) {
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            return create(field, message);
        }
        public static ValidationError of(final ConstraintViolation<?> fieldError) {
            String field = String.valueOf(fieldError.getPropertyPath());
            String message = fieldError.getMessage();
            return create(field, message);
        }
        private static ValidationError create(String field, String message) {
            return new ValidationError(field, message);
        }
    }
}