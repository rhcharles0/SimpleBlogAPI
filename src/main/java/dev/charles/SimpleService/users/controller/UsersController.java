package dev.charles.SimpleService.users.controller;

import dev.charles.SimpleService.users.dto.UserDto;
import dev.charles.SimpleService.users.service.UsersService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class UsersController {
    final private UsersService usersService;

    /**
     * GET /api/users/{email}
     * 사용자의 정보 반환
     * @return 사용자 데이터 (UserDto)
     */
    @GetMapping
    ResponseEntity<UserDto> getUser(
            @RequestParam
            @Email(
                    regexp = "^[a-zA-Z][a-zA-Z0-9]{4,}@[a-zA-Z]+\\.[a-z]{2,}$",
                    message = "올바른 이메일을 입력하세요."
            )
            @NotBlank(message = "이메일은 필수입니다.")
            String email){
        UserDto userDto =  usersService.getUserByEmail(email);
        return new ResponseEntity<>(userDto,HttpStatus.OK);
    }

    /**
     * GET /api/users/paged
     * 사용자의 목록을 페이징 처리하여 반환합니다.
     * @return 페이징된 사용자 데이터 (Page<UserDto>)
     */

    @GetMapping("/paged")
    ResponseEntity<Page<UserDto>> getUsers(
            @NotNull @RequestParam(value = "isSearchMode") Boolean isSearchMode,
            @RequestParam(value = "keyword", required = false, defaultValue = "")
            final String keyword,
            @Min(value = 0, message = "최소 0 이상입니다.")
            @RequestParam(value = "pageNumber", defaultValue = "0")
            final Integer pageNumber){
        Page<UserDto> users = usersService.getUsers(isSearchMode, keyword, pageNumber);
        return new ResponseEntity<>(users,HttpStatus.OK);
    }

    /**
     * POST /api/users
     * 새로운 사용자 계정을 생성합니다.
     * @param userDto 생성할 사용자 정보 (username, email)
     * @return HTTP 201 CREATED 반환
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Validated @RequestBody UserDto userDto) {
        // Service 계층의 생성 메서드 호출
        usersService.create(userDto);
        // HTTP 201 Created와 함께 생성된 사용자 정보를 반환
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }


    /**
     * PUT /api/users
     * 특정 ID를 가진 사용자 정보를 수정합니다.
     * @param userDto 수정할 사용자 정보 (username, email)
     * @return 수정된 사용자 정보 (UserDto)
     */
    @PutMapping
    public ResponseEntity<?> updateUser(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @Validated @RequestBody UserDto userDto) {

        usersService.update(principal.getAttribute("email"), userDto);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    /**
     * DELETE /api/users
     * 특정 ID를 가진 사용자 계정을 삭제합니다.
     * @return 응답 본문 없이 HTTP 204 No Content 반환
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {
        usersService.delete(principal.getAttribute("email"));
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }



}
