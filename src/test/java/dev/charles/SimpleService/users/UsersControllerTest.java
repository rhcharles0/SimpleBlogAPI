package dev.charles.SimpleService.users;

import dev.charles.SimpleService.errors.errorcode.CommonErrorCode;
import dev.charles.SimpleService.errors.errorcode.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
@DisplayName("UsersController Test")
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTP 요청 시뮬레이션을 위한 객체


    private ObjectMapper objectMapper = new ObjectMapper(); // JSON 직렬화를 위한 객체

    @MockitoBean
    private UsersService usersService; // Controller의 의존성 Mocking

    private final UserDto testUserDto = new UserDto( "tesdt@example.com","tester");
    private final String targetEmail = "tesdt@example.com";

    @BeforeEach
    void setup() {

    }

    @Test
    @DisplayName("When invoking GET /api/users, the response status is 200 OK and the response body contains the User DTO .")
    void get_user_by_email() throws Exception {
        // Given
        given(usersService.getUserByEmail(targetEmail)).willReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/api/users").with(opaqueToken())
                        .param("email", targetEmail)
                        .accept(MediaType.APPLICATION_JSON)) // JSON 응답을 기대
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.email").value(targetEmail));

        // Service 호출 검증
        then(usersService).should().getUserByEmail(targetEmail);
    }

    @Test
    @DisplayName("When invoking GET /api/users/paged, the response status is 200 OK and the response body contains the paged user list.")
    void get_paged_users_by_offset() throws Exception {
        // Given
        final int offset = 0;
        final int pageSize = 10;
        final PageRequest pageable = PageRequest.of(offset, pageSize);
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        List<UserDto> userList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            userList.add(new UserDto(String.format("test%d", i),  String.format("test%d@gmail.com", i)));
        }
        Page<UserDto> mockPage = new PageImpl<>(userList, pageable, 10L);
        params.add("offset", "0");
        params.add("keyword", "test");
        given(usersService.getUsers("test", offset, null))
                .willReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/users/paged").with(opaqueToken())
                        .params(params)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(10)); // 페이징 메타데이터 검증

        // Service 호출 검증 (offset=1로 호출되었는지)
        then(usersService).should().getUsers("test", offset, null);
    }

    @Test
    @DisplayName("When invoking POST /api/users, the response status is 201 CREATED and the corresponding Service method is called.")
    void create_user_with_valid_data() throws Exception {
        // Given
        // usersService.create()는 void (혹은 DTO 반환)이며, 여기서는 서비스 호출만 검증
        // Controller 코드가 new ResponseEntity<>(null, HttpStatus.CREATED)를 반환하므로 DTO 반환은 테스트하지 않음.

        // When & Then
        mockMvc.perform(post("/api/users").with(opaqueToken()
                                .attributes(attrs ->
                                        attrs.put("email", targetEmail)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(testUserDto))
                ) // DTO를 JSON 본문으로 전송
                .andExpect(status().isCreated()); // 201 CREATED 상태 확인

        // Service 호출 검증
//        then(usersService).should().create(testUserDto);
        verify(usersService, times(1)).create(any());
    }

    @Test
    @DisplayName("When invoking POST /api/users with an invalid User DTO, the response status is 400 BAD REQUEST.")
    void create_user_with_invalid_data() throws Exception {
        // Given
        UserDto invalid = new UserDto("d", "d");

        ErrorCode error = CommonErrorCode.INVALID_PARAMETER;
        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(opaqueToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(invalid))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(error.name()))
                .andExpect(jsonPath("$.message").value(error.getMessage()))
                .andExpect(jsonPath("$.errors[?(@.message == '올바른 이름을 입력하세요.')]").exists())
                .andExpect(jsonPath("$.errors[?(@.message == '5이상 15이하 글자를 입력하세요.')]").exists());

        then(usersService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("When invoking PUT /api/users, the response status is 200 OK and the response body contains the paged user list.")
    void update_user_with_valid_data() throws Exception {
        // Given
        UserDto updatedDto = new UserDto(targetEmail, "updatedName");
        // Controller의 updateUser는 PathVariable의 "id"를 String email로 받습니다.
//        given(usersService.update(targetEmail, updatedDto)).willReturn(updatedDto);

        // When & Then
        mockMvc.perform(put("/api/users")
                        .with(opaqueToken()
                                .attributes(attrs -> attrs
                                        .put("email", targetEmail)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isNoContent());

        verify(usersService, times(1)).update(any(), any());


    }

    @Test
    @DisplayName("When invoking PUT /api/users/{id}, a validation error results in a 400 BAD REQUEST status.")
    void update_user_with_invalid_data() throws Exception {
        // Given
        UserDto updatedDto = new UserDto("up", targetEmail);
        ErrorCode INVALID_PARAMETER = CommonErrorCode.INVALID_PARAMETER;
        // When & Then
        mockMvc.perform(put("/api/users")
                        .with(opaqueToken()
                                .attributes(attrs -> attrs
                                        .put("email", targetEmail)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_PARAMETER.name()))
                .andExpect(jsonPath("$.message").value(INVALID_PARAMETER.getMessage()))
                .andExpect(jsonPath("$.errors[?(@.message == '올바른 이름을 입력하세요.')]").exists())
                .andExpect(jsonPath("$.errors[?(@.message == '5이상 15이하 글자를 입력하세요.')]").exists());

        then(usersService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("When invoking DELETE /api/users/{email}, the response status is 204 NO CONTENT and the corresponding Service method is called.")
    void delete_logged_User_In() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users").with(opaqueToken()
                        .attributes(attrs -> attrs
                                .put("email", targetEmail))))
                .andExpect(status().isNoContent()); // 204 NO CONTENT 상태 확인

        // Service 호출 검증
        then(usersService).should().delete(targetEmail);
    }
}