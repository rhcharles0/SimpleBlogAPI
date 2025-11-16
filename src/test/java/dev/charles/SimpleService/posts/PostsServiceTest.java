package dev.charles.SimpleService.posts;

import dev.charles.SimpleService.posts.domain.Posts;
import dev.charles.SimpleService.posts.dto.PostDto;
import dev.charles.SimpleService.posts.repository.PostsRepository;
import dev.charles.SimpleService.posts.service.PostsService;
import dev.charles.SimpleService.users.domain.Users;
import dev.charles.SimpleService.users.dto.UserDto;
import dev.charles.SimpleService.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostsServiceTest {
    @Mock
    private PostsRepository postsRepository;
    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private PostsService postsService;

    private Users user;
    private Posts post;
    private UserDto userDto;
    private PostDto postDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .email("userdsd@email.com")
                .username("user").build();
        postDto = PostDto.builder()
                .title("post")
                .content("content").build();
        user = Users.of(userDto);
        post = Posts.of(postDto);
        post.setUser(user);
    }
    @Test
    void createPost() {
        //given
        given(usersRepository.findByEmail(any())).willReturn(Optional.of(user));
        String email = "email@gmail.com";
        //when
        postsService.createPost(email, postDto);

        //then
        verify(usersRepository, times(1)).findByEmail(any());
        verify(postsRepository, times(1)).save(any());
    }

    @Test
    void getAllPostsByKeyword() {
        //given
        String keyword = "hi";
        List<PostDto> dtoList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            PostDto temp = PostDto.builder().title("hi"+i)
                            .content("content")
                                    .build();
            dtoList.add(temp);
        }
        given(postsRepository.countByKeyword(any())).willReturn(5L);
        given(postsRepository.findAllByKeyword(any(), any())).willReturn(dtoList);

        //when
        Page<PostDto> result = postsService.getAllPosts(keyword, 0, null);

        //then
        verify(postsRepository, times(1)).countByKeyword(any());
        verify(postsRepository, times(1)).findAllByKeyword(any(), any());

        dtoList.forEach(dto -> {
            assertThat(result.get()).contains(dto);
        });
    }

    @Test
    void getAllPostsByKeywordAndUser() {
        //given
        String keyword = "hi";
        List<PostDto> dtoList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            PostDto temp = PostDto.builder().title("hi"+i)
                    .content("content")
                    .build();
            dtoList.add(temp);
        }
        given(postsRepository.countByKeywordAndEmail(any(), any()) ).willReturn(5L);
        given(postsRepository.findAllByKeywordAndEmail(any(), any(), any()) ).willReturn(dtoList);

        //when
        Page<PostDto> result = postsService.getAllPostsbyUser("dd", keyword, 5,null);

        //then
        verify(postsRepository, times(1)).countByKeywordAndEmail(any(), any() );
        verify(postsRepository, times(1)).findAllByKeywordAndEmail(any(),any(), any()) ;

        dtoList.forEach(dto -> {
            assertThat(result.get()).contains(dto);
        });
    }

    @Test
    void getPostById() {
        //given
        given(postsRepository.findById(any(), any())).willReturn(Optional.of(postDto));
        //when
        PostDto result = postsService.getPostById(1L);
        //then
        assertAll(
                ()-> assertThat(result.getTitle()).isEqualTo(post.getTitle()),
                ()-> assertThat(result.getContent()).isEqualTo(post.getContent())
        );
        verify(postsRepository, times(1)).findById(any(), any());
    }

    @Test
    void updatePost() {
        //given
        given(postsRepository.findById(any())).willReturn(Optional.of(post));
        PostDto newDto = PostDto.builder()
                .title("new title")
                .content("content sdsd")
                .build();
        //when
        postsService.updatePost(1L, newDto);

        //then
        assertAll(
                ()-> assertThat(post.getTitle()).isEqualTo(newDto.getTitle()),
                ()-> assertThat(post.getContent()).isEqualTo(newDto.getContent())
        );
        verify(postsRepository, times(1)).findById(any());
    }

    @Test
    void deletePost() {
        // when
        postsService.deletePost(1L);
        // then
        verify(postsRepository, times(1)).deleteById(any());
    }
}