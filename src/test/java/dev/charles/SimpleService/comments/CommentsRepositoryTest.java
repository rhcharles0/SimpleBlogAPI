package dev.charles.SimpleService.comments;

import dev.charles.SimpleService.AbstractIntegrationTest;
import dev.charles.SimpleService.comments.domain.Comments;
import dev.charles.SimpleService.comments.dto.CommentsRequestDto;
import dev.charles.SimpleService.comments.dto.CommentsResponseDto;
import dev.charles.SimpleService.comments.repository.CommentsRepository;
import dev.charles.SimpleService.posts.domain.Posts;
import dev.charles.SimpleService.posts.dto.PostDto;
import dev.charles.SimpleService.posts.repository.PostsRepository;
import dev.charles.SimpleService.users.domain.Users;
import dev.charles.SimpleService.users.dto.UserDto;
import dev.charles.SimpleService.users.repository.UsersRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class CommentsRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private CommentsRepository commentsRepository;
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private UsersRepository usersRepository;

    @DisplayName("Given 1 user, 5 posts, and 5 parentComments which of one has 3 replies ")
    @Nested
    class ReadTest{
        private Posts curPost;
        private Users curUser;
        private Comments parentComment;
        private Pageable pageable;
        @BeforeEach
        void setup(){
            UserDto userDto = UserDto.builder()
                    .email("test@email.com")
                    .username("test").build();
            curUser = Users.of(userDto);
            usersRepository.save(curUser);
            for (int i = 5; i >= 1; i--) {
                PostDto temp = PostDto.builder().title("test"+i)
                        .content("content").build();
                Posts post = Posts.of(temp);
                curPost =postsRepository.save(post);
            }
            for (int i = 5; i >= 1; i--) {
                Comments comment = Comments.builder()
                        .post(curPost)
                        .user(curUser)
                        .content("comment"+i).build();
                parentComment =commentsRepository.save(comment);
            }
            // replies

            for (int i = 3; i >= 1; i--) {
                CommentsRequestDto temp = CommentsRequestDto.builder().content("replies"+i)
                        .parentId(1L).build();
                Comments comment = Comments.builder()
                        .parentComment(parentComment)
                        .post(curPost)
                        .user(curUser)
                        .content("reply"+i).build();
                commentsRepository.save(comment);
            }

            pageable = PageRequest.of(0,10);
        }

        @Nested
        @DisplayName("When we have postId")
        class PostId{
            private Long postId;
            @BeforeEach
            void setup(){
                postId = curPost.getId();
            }
            @Test
            @DisplayName("Then you can get only parent comments")
            void findAllParentsByPostId() {
                List<CommentsResponseDto> result =  commentsRepository.findAllParentsByPostId(postId, pageable);
                assertSoftly((softly)-> {
                    softly.assertThat(result.size()).isEqualTo(5);
                    softly.assertThat(result.get(0).getContent()).isEqualTo("comment1");
                });
            }

            @Test
            @DisplayName("Then you can get count of parent comments with postId")
            void countParentsByPostId(){
                Long result = commentsRepository.countParentsByPostId(postId);
                assertThat(result).isEqualTo(5);
            }
        }

        @Nested
        @DisplayName("When we have parentId")
        class ParentId{
            private Long parentId;
            @BeforeEach
            void setup(){
                parentId = parentComment.getId();
            }
            @Test
            @DisplayName("Then you can have replies with parentId")
            void findAllParentsByPostId() {
                List<CommentsResponseDto> result =  commentsRepository.findAllChildrenByParentId(parentId, pageable);
                assertSoftly((softly)-> {
                    softly.assertThat(result.size()).isEqualTo(3);
                    softly.assertThat(result)
                            .extracting(CommentsResponseDto::getContent)
                            .allMatch(content -> content.contains("reply"));
                });
            }
            @Test
            @DisplayName("Then you can get count of child comments with parentId")
            void countParentsByPostId(){
                Long result = commentsRepository.countChildrenByParentId(parentId);
                assertThat(result).isEqualTo(3);
            }
        }

    }

    @DisplayName("Given 1 user,1 post,and 1 parentComment ")
    @Nested
    class UpdateAndDeleteTest{
        private Posts curPost;
        private Users curUser;
        private Comments savedComment;
        private String newCotent;
        @BeforeEach
        void setup(){
            UserDto userDto = UserDto.builder()
                    .email("test@email.com")
                    .username("test").build();
            curUser = Users.of(userDto);
            usersRepository.save(curUser);
            PostDto temp = PostDto.builder().title("test")
                        .content("content").build();
            Posts post = Posts.of(temp);
            curPost =postsRepository.save(post);

            Comments comment = Comments.builder()
                    .post(curPost)
                    .user(curUser)
                    .content("comment").build();
            savedComment = commentsRepository.save(comment);
            newCotent = "new comment";
        }

        @AfterEach
        void tearDown(){
            commentsRepository.deleteAll();
        }

        @Nested
        @DisplayName("When we have a post, user, and content")
        class newComment{
            @Test
            @DisplayName("Then you can delete a existed comment")
            void delete(){
                commentsRepository.delete(savedComment);
                System.out.println(commentsRepository.findAll());
                assertThat(commentsRepository.count()).isEqualTo(0);
            }

            @Test
            @DisplayName("Then you can create a comment with post, user, and content")
            void create(){
                Comments newComment = Comments.builder()
                        .post(curPost)
                        .user(curUser)
                        .content(newCotent)
                        .build();
                Comments saved = commentsRepository.save(newComment);

                assertThat(commentsRepository.count()).isEqualTo(2);

            }


        }

    }



}
