package dev.charles.SimpleService.users;

import dev.charles.SimpleService.errors.exception.DuplicateResourceException;
import dev.charles.SimpleService.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsersService {
    final private UsersRepository usersRepository;

    UserDto getUserByEmail (String email){
        return usersRepository.findByEmail(email, UserDto.class).orElseThrow(
                () -> new NotFoundResourceException("Not found user by email")
        );
    }
    Page<UserDto> getUsers(final Integer offset){
        int pageSize = 10;
        Pageable pageable = PageRequest.of(offset,pageSize);
        return usersRepository.findAllByOrderByCreatedAtDesc(pageable);

    }

    @Modifying
    @Transactional
    void create(final UserDto userDto){
        if(usersRepository.findByEmail(userDto.email())
                .isPresent()){
            throw new DuplicateResourceException("Already user existed by email");
        }
        Users user = Users.builder()
                .username(userDto.username())
                .email(userDto.email()).build();
        usersRepository.save(user);
    }

    void delete(final String email){
        Users user = usersRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundResourceException("Not found user by email")
        );
        usersRepository.delete(user);
    }

    UserDto update(final String email, final UserDto userDto){
        Users user = usersRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundResourceException("Not found user by email")
        );
        user.update(userDto);
        usersRepository.save(user);

        return userDto;
    }

}

