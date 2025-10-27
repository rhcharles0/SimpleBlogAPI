package dev.charles.SimpleService.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    <T> Optional<T> findByEmail(String email, Class<T> type);
    <T> Optional<T> findById(Long id, Class<T> type);
    Page<UserDto> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
