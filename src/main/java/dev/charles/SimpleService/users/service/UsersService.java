package dev.charles.SimpleService.users.service;

import dev.charles.SimpleService.errors.exception.DuplicateResourceException;
import dev.charles.SimpleService.errors.exception.NotFoundResourceException;
import dev.charles.SimpleService.users.dto.UserDto;
import dev.charles.SimpleService.users.domain.Users;
import dev.charles.SimpleService.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsersService {
    final private UsersRepository usersRepository;

    public UserDto getUserByEmail (String email){
        return usersRepository.findByEmail(email, UserDto.class).orElseThrow(
                () -> new NotFoundResourceException("Not found user by email")
        );
    }

    public Page<UserDto> getUsers(final String keyword, final Integer pageNumber, final Long total){
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<UserDto> userDtoList =  usersRepository.findAllByKeyword(keyword, pageable);
        long totalCount = Optional.ofNullable(total).orElseGet(()->usersRepository.countByKeyword(keyword));
        return new PageImpl<>(userDtoList, pageable, totalCount);
    }

    @Transactional
    @PreAuthorize("principal.claims['email'] == #userDto.email")
    public void create(final UserDto userDto){
        if(isPresent(userDto.getEmail())){
            throw new DuplicateResourceException("Already user existed by email");
        }
        Users user = Users.of(userDto);
        usersRepository.save(user);
    }

    @Transactional
    public void delete(final String email){
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundResourceException("Not found user by email"));
        usersRepository.delete(user);
    }

    @Transactional
    public void update(final String email, final UserDto userDto){
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundResourceException("Not found user by email"));
        if(isPresent(userDto.getEmail())){
            throw new DuplicateResourceException("Already user existed by email");
        }
        user.update(userDto);
    }

    private boolean isPresent(String email){
        return usersRepository.findByEmail(email).isPresent();
    }

}

