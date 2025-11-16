package dev.charles.SimpleService.posts;

import dev.charles.SimpleService.AbstractIntegrationTest;
import dev.charles.SimpleService.posts.domain.Posts;
import dev.charles.SimpleService.posts.dto.PostDto;
import dev.charles.SimpleService.posts.repository.PostsRepository;
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

public class PostsRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private UsersRepository usersRepository;

    private PostDto postDto = PostDto.builder()
            .title("post0")
            .content("content0").build();

    @BeforeEach
    public void setUp(){
        //given
        postsRepository.deleteAll();
        usersRepository.deleteAll();
        UserDto userDto1 = UserDto.builder()
                .username("test1")
                .email("hi@email.com").build();
        UserDto userDto2 = UserDto.builder()
                .username("test2")
                .email("test2@email.com").build();
        Users user1 = Users.of(userDto1);
        Users user2 = Users.of(userDto2);
        usersRepository.save(user1);
        usersRepository.save(user2);
        for (int i = 0; i < 5; i++) {
            PostDto tempDto = PostDto.builder().content("content"+i)
                    .title("post"+i).build();
            Posts post = Posts.of(tempDto);
            post.setUser(i%2== 0 ? user1 : user2);
            postsRepository.save(post);
        }
    }

    @Test
    @DisplayName("Get post by Id")
    void getPostTest(){
        //given
        Long id = postsRepository.findAll().get(0).getId();
        //when
        Posts newPostDto = postsRepository.findById(id)
                .orElseThrow();
        //then
        assertThat(newPostDto)
                .extracting("title", "content")
                .contains(postDto.getContent(),postDto.getTitle());
    }

    @Test
    @DisplayName("Get postDto by Id")
    void getPostDtoTest(){
        //given
        Long id = postsRepository.findAll().get(0).getId();
        //when
        Optional<PostDto> newPostDto = postsRepository.findById(id, PostDto.class);
        //then
        assertThat(newPostDto.get())
                .extracting("title", "content")
                .contains(postDto.getContent(),postDto.getTitle());
    }
    @Test
    @DisplayName("Delete post by Id")
    void deletePostbyIdTest(){
        //given
        Long id = postsRepository.findAll().get(0).getId();
        //when
        postsRepository.deleteById(id);

        //then
        assertThat(postsRepository.count()).isEqualTo(4L);
    }

    @Test
    @DisplayName("Save post")
    void savePost() {
        //given
        postsRepository.deleteAll();
        // when
        postsRepository.save(Posts.of(postDto));
        // then
        assertThat(postsRepository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("get pagination of postDto by keyword")
    void findAllByKeywordTest() {
        // given
        Pageable pageable = PageRequest.of(0,5);
        String keyword = "pos";
        // when
        List<PostDto> postDtoList = postsRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(postDtoList.size()).isEqualTo(5);
    }

    @Test
    @DisplayName("get pagination of postDto  by keyword and email")
    void findAllByKeywordByEmailTest() {
        // given
        Pageable pageable = PageRequest.of(0,5);
        String keyword = "pos";
        String email = "hi@email.com";
        // when
        List<PostDto> postDtoList = postsRepository.findAllByKeywordAndEmail(keyword, email, pageable);

        // then
        assertThat(postDtoList.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("Get count of posts by keyword")
    void countByKeyword() {
        // given
        String keyword = "pos";
        // when
        Long result = postsRepository.countByKeyword(keyword);

        // then
        assertThat(result).isEqualTo(5);
    }
    @Test
    @DisplayName("Get count of posts by keyword and email")
    void countByKeywordAndEmail() {
        // given
        String keyword = "pos";
        String email = "hi@email.com";
        // when
        Long result = postsRepository.countByKeywordAndEmail(keyword, email);

        // then
        assertThat(result).isEqualTo(3);
    }


}
