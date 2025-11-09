package dev.charles.SimpleService.users.repository;

import dev.charles.SimpleService.users.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long>, CustomizedUserRepository {
    Optional<Users> findByEmail(String email);
    <T> Optional<T> findByEmail(String email, Class<T> type);

}
