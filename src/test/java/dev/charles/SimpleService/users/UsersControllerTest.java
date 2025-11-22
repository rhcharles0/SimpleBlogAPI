package dev.charles.SimpleService.users;

import dev.charles.SimpleService.AbstractIntegrationTest;
import dev.charles.SimpleService.errors.errorcode.CommonErrorCode;
import dev.charles.SimpleService.errors.errorcode.ErrorCode;
import dev.charles.SimpleService.users.dto.UserDto;
import dev.charles.SimpleService.users.service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class UsersControllerTest extends AbstractIntegrationTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UsersService usersService;

    private final UserDto testUserDto = new UserDto( "tesdt@example.com","tester");
    private final String targetEmail = "tesdt@example.com";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Nested
    @DisplayName("Given we have a invalid email and a valid email")
    class GetUserTest{
        private String validEmail = "simple@email.mail";
        private String invalidEmail = "sim";
        private UserDto userDto;
        @BeforeEach
        void setup(){
            userDto = new UserDto(validEmail, "simple");
            given(usersService.getUserByEmail(validEmail)).willReturn(userDto);
        }
        @Nested
        @DisplayName("When invoking GET /api/users with a valid email")
        class accessWithValidEmail{
            @Test
            @DisplayName("Then the response status is 200 OK and the response body contains the User DTO .")
            void getUserDto() throws Exception {
                mockMvc.perform(get("/api/users").with(opaqueToken())
                                .param("email", validEmail)
                                .accept(MediaType.APPLICATION_JSON)) // JSON 응답을 기대
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.username").value(userDto.getUsername()))
                        .andExpect(jsonPath("$.email").value(userDto.getEmail()));
                verify(usersService, times(1)).getUserByEmail(validEmail);
            }
        }
        @Nested
        @DisplayName("When invoking GET /api/users with a invalid email")
        class accessWithInvalidEmail{
            @Test
            @DisplayName("Then the response status is 400.")
            void getUserDto() throws Exception {
                mockMvc.perform(get("/api/users").with(opaqueToken())
                                .param("email", invalidEmail)
                                .accept(MediaType.APPLICATION_JSON)) // JSON 응답을 기대
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, times(0)).getUserByEmail(invalidEmail);
            }
        }

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
        params.add("isSearchMode", "false");
        params.add("keyword", "test");
        given(usersService.getUsers( false, "test", offset))
                .willReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/users/paged")
                        .params(params)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(10)); // 페이징 메타데이터 검증

        // Service 호출 검증 (offset=1로 호출되었는지)
        then(usersService).should().getUsers(false, "test", offset);
    }

    @Nested
    @DisplayName("Given we have both invalid and valid DTOs.")
    class CreateUserTest{
        private String email;
        private UserDto validDto;
        private UserDto invalidDto;

        @BeforeEach
        void setup(){
            email = "simple@email.com";
            validDto = UserDto.builder().email(email).username("simple").build();
            invalidDto = UserDto.builder().email(email).username("a").build();
        }
        @Nested
        @DisplayName("When invoking POST /api/users with a valid dto")
        class accessWithValidDto{
            @Test
            @DisplayName("Then the response is 201 and user is created")
            void createUser() throws Exception {
                mockMvc.perform(post("/api/users").with(opaqueToken()
                                        .attributes(attrs ->
                                                attrs.put("email", email)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper
                                        .writeValueAsString(validDto))
                        )
                        .andExpect(status().isCreated());
                verify(usersService, times(1)).create(argThat(
                        userDto -> userDto.getEmail().equals(email) && userDto.getUsername().equals(validDto.getUsername())
                ));
            }
        }

        @Nested
        @DisplayName("When invoking POST /api/users with a invalid dto")
        class accessWithInvalidDto{
            @Test
            @DisplayName("Then the response is 400 ")
            void createUser() throws Exception {
                mockMvc.perform(post("/api/users").with(opaqueToken()
                                        .attributes(attrs ->
                                                attrs.put("email", email)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper
                                        .writeValueAsString(invalidDto))
                        )
                        .andExpect(status().isBadRequest());
                verify(usersService, times(0)).create(argThat(
                        userDto -> userDto.getEmail().equals(email) && userDto.getUsername().equals(invalidDto.getUsername())
                ));
            }
        }
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
                .andExpect(jsonPath("$.errors[?(@.message == '2이상 15이하 글자를 입력하세요.')]").exists());

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