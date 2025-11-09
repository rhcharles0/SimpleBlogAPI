package dev.charles.SimpleService.users;

import dev.charles.SimpleService.AbstractIntegrationTest;
import dev.charles.SimpleService.users.domain.Users;
import dev.charles.SimpleService.users.dto.UserDto;
import dev.charles.SimpleService.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UsersRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private UsersRepository usersRepository;

    private Users user1;
    private Users user2;

    @BeforeEach
    void setUp() {
        usersRepository.deleteAll();
        UserDto userDto1 = new UserDto("test1@email.com","user1");
        UserDto userDto2 = new UserDto("test2@email.com","user2");
        user1 = Users.of(userDto1);
        user2 = Users.of(userDto2);
        usersRepository.save(user1);
        usersRepository.save(user2);
    }

    @Test
    @DisplayName("Save user ")
    void save() {
        UserDto userDto = new UserDto("test3@email.com","user3");
        Users user = Users.of(userDto);

        usersRepository.save(user);

        assertThat(usersRepository.count()).isEqualTo(3);
    }


    @Test
    @DisplayName("Get a user by email")
    void findByEmail_shouldReturnUsersEntity() {
        // When
        Users foundUser = usersRepository.findByEmail("test1@email.com").orElseThrow();

        // Then
        assertThat(foundUser.getEmail()).isEqualTo("test1@email.com");
        assertThat(foundUser.getUsername()).isEqualTo("user1");
    }

    @Test
    @DisplayName("Get pagination of users that is sorted by creation date descending (newest first)")
    void findAllByOrderByCreatedAtDesc_shouldReturnPagedData() {
        // Given
        Pageable pageable = PageRequest.of(0, 5); // 0페이지, 사이즈 1
        // When
        List<UserDto> userPage = usersRepository.findAllByKeyword("", pageable);
        // Then
        assertThat(userPage.size()).isEqualTo(2);
        assertThat(userPage.get(0)).extracting("email","username")
                .contains("test2@email.com","user2");
    }

    @Test
    @DisplayName("Delete user from repository with having two users")
    void delete_user() {
        Users seleted = usersRepository.findByEmail("test1@email.com").orElseThrow();
        usersRepository.delete(seleted);

        assertThat(usersRepository.count()).isEqualTo(1);
    }

}
