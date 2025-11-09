package dev.charles.SimpleService.users;


import dev.charles.SimpleService.users.domain.Users;
import dev.charles.SimpleService.users.dto.UserDto;
import dev.charles.SimpleService.users.repository.UsersRepository;
import dev.charles.SimpleService.users.service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest{
    @InjectMocks
    private UsersService usersService;
    @Mock
    private UsersRepository usersRepository;

    @Nested
    @DisplayName("Given there are two registered users")
    public class RegisteredTwoUsersTest{
        //GIVEN
        private UserDto mikeDto;
        private Users mike;

        @BeforeEach
        void setup(){
            mikeDto = new UserDto("mike","mike@gmail.com");
            mike = Users.of(mikeDto);
        }
        @Nested
        @DisplayName("When use service")
        class GetMikeEntityByEmailTest{
            @Test
            @DisplayName("Then  find mike of a user entity by email and repository is called ")
            public void UserGetTest() {
                //given
                String targetEmail = "mike@gmail.com";
                given(usersRepository.findByEmail("mike@gmail.com", UserDto.class))
                        .willReturn(Optional.of(mikeDto));
                //when
                UserDto result = usersService.getUserByEmail(targetEmail);
                // then
                verify(usersRepository, times(1)).findByEmail(targetEmail, UserDto.class);
                assertThat(result).extracting("email", "username")
                        .contains("mike","mike@gmail.com");
            }
            @Test
            @DisplayName("Then the repository is called with correct Pageable and returns the Page")
            void getUsers_ShouldCallRepositoryWithCorrectPageable() {
                Page<UserDto> mockPage;
                int targetOffset = 0; // 3번째 페이지 (인덱스 2)
                final int pageSize = 10;
                // When: 3번째 페이지 (offset=2) 요청
                List<UserDto> pageContent = List.of(
                        new UserDto("user1", "u1@mail.com"),
                        new UserDto("user2", "u2@mail.com"),
                        new UserDto("user3", "u3@mail.com"),
                        new UserDto("user4", "u4@mail.com"),
                        new UserDto("user5", "u5@mail.com"),
                        new UserDto("user6", "u6@mail.com"),
                        new UserDto("user7", "u7@mail.com"),
                        new UserDto("user8", "u8@mail.com"),
                        new UserDto("user9", "u9@mail.com"),
                        new UserDto("user10", "u10@mail.com")
                );
                // Given: Repository가 targetOffset으로 호출되면 mockPage를 반환하도록 설정
                given(usersRepository.findAllByKeyword("user",
                        PageRequest.of(targetOffset, pageSize)
                )).willReturn(pageContent);

                Page<UserDto> resultPage = usersService.getUsers("user", targetOffset, null);

                assertThat(resultPage.getTotalElements()).as("Total Elements").isEqualTo(10);
                assertThat(resultPage.getNumberOfElements()).as("Number of Elements").isEqualTo(10);
            }
            @Test
            @DisplayName("Then the repository's save method is called with a Users entity")
            void UserCreateTest() {
                // when
                usersService.create(mikeDto);
                // then
                verify(usersRepository).save(any());
            }

            @Test
            @DisplayName("Then find the user entity by email and then the entity is deleted")
            void UserDeleteTest() {
                //given
                given(usersRepository.findByEmail(any())).willReturn(Optional.of(mike));
                // when
                usersService.delete("mike@gmail.com");
                // then
                verify(usersRepository, times(1)).delete(any());
            }

            @Test
            @DisplayName("Then find the user by email and update the user properties by given parameters")
            void UserUpdate(){
                //given
                UserDto newDto = new UserDto("mike2", "mike2@gmail.com");
                given(usersRepository.findByEmail("mike@gmail.com")).willReturn(Optional.ofNullable(mike));
                // when
                usersService.update("mike@gmail.com", newDto);
                // then
                assertThat(mike.getEmail()).isEqualTo(newDto.getEmail());
                assertThat(mike.getUsername()).isEqualTo(newDto.getUsername());
                verify(usersRepository, times(2)).findByEmail(any());
            }



        }

    }


}