package dev.charles.SimpleService.users;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record UserDto(
        @Length(min = 5, max = 15, message = "5이상 15이하 글자를 입력하세요.")
        @Pattern(regexp = "^[a-zA-Z가-힣][a-zA-Z가-힣0-9]{4,}$", message = "올바른 이름을 입력하세요.")
        @NotBlank(message = "사용자 이름은 필수입니다.")
        @JsonProperty
        String username,

        @Email(
                regexp = "^[a-zA-Z][a-zA-Z0-9]{4,}@[a-zA-Z]+\\.[a-z]{2,}$",
                message = "올바른 이메일을 입력하세요."
        )
        @NotBlank(message = "이메일은 필수입니다.")
        @JsonProperty
        String email) {
}
