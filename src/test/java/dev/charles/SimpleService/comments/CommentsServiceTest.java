package dev.charles.SimpleService.comments;

import dev.charles.SimpleService.comments.domain.Comments;
import dev.charles.SimpleService.comments.dto.CommentsRequestDto;
import dev.charles.SimpleService.comments.repository.CommentsRepository;
import dev.charles.SimpleService.comments.service.CommentsService;
import dev.charles.SimpleService.posts.domain.Posts;
import dev.charles.SimpleService.posts.dto.PostDto;
import dev.charles.SimpleService.posts.repository.PostsRepository;
import dev.charles.SimpleService.users.domain.Users;
import dev.charles.SimpleService.users.dto.UserDto;
import dev.charles.SimpleService.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentsServiceTest {
    @Mock
    private CommentsRepository commentsRepository;
    @Mock
    private PostsRepository postsRepository;
    @Mock
    private UsersRepository usersRepository;
    @InjectMocks
    private CommentsService commentsService;

    @Nested
    @DisplayName("Given we have 1 user and 2 posts which of one has 11 comments")
    class getPaginationTest{
        private Long postId;
        private Long parentId;
        private Integer pageNumber;

        @BeforeEach
        void setup(){
            postId = 11L;
            parentId = 2L;
            pageNumber = 0;
        }

        @Nested
        @DisplayName("When we have a postId")
        class postId{
            @Test
            @DisplayName("Then you can retrieve a pagination of parent comments ranging from 0 to 10, sorted by creation date in descending order")
            void getCommentsByPostId(){
                commentsService.getCommentsByPostId(postId, pageNumber, null);
                verify(commentsRepository, times(1)).findAllParentsByPostId(eq(postId), argThat(pageable ->
                        pageable.getPageNumber() == 0 && pageable.getPageSize() == 10));
                verify(commentsRepository, times(1)).countParentsByPostId(postId);
            }
        }

        @Nested
        @DisplayName("When we have a parentId")
        class parentId{
            @Test
            @DisplayName("Then you can retrieve a pagination of child comments ranging from 0 to 10, sorted by creation date in descending order")
            void getRepliesByParentId(){
                commentsService.getRepliesByParentId(parentId, pageNumber, null);
                verify(commentsRepository, times(1)).findAllChildrenByParentId(eq(parentId), argThat(pageable ->
                        pageable.getPageNumber() == 0 && pageable.getPageSize() == 10));
                verify(commentsRepository, times(1)).countChildrenByParentId(parentId);
            }
        }
    }

    @Nested
    @DisplayName("Given we have a post , a user, and a text")
    class UpdateAndDeleteCommentTest{
        String email;
        Long commentId;
        String newText;
        Comments comment;
        Users user;

        @BeforeEach
        void setup(){
            email = "sample@email.com";
            UserDto userDto = UserDto.builder()
                    .username("test")
                    .email(email).build();
            user = Users.of(userDto);
            comment = Comments.builder()
                    .content("")
                    .user(user).build();
            given(commentsRepository.findById(commentId)).willReturn(Optional.of(comment));
        }

        @Nested
        @DisplayName("When we have a new text, a comment id, and email")
        class UpdateProperty{
            @Test
            @DisplayName("Then you can update your comment with a new text")
            void updateComment(){
                commentsService.updateComment(commentId, newText, email);
                verify(commentsRepository, times(1)).findById(commentId);
            }
        }

        @Nested
        @DisplayName("When we have  a comment id and email")
        class DeleteProperty{
            @Test
            @DisplayName("Then you can delete your comment")
            void updateComment(){
                commentsService.deleteComment(commentId, email);
                verify(commentsRepository, times(1)).findById(commentId);
                verify(commentsRepository, times(1)).delete(comment);
            }
        }
    }

    @Nested
    @DisplayName("Given we have a user and a post")
    class createCommentTest{
        private Users user;
        private String email;
        private String content;
        private Posts post;

        @BeforeEach
        void setup(){
            email = "sample@email.com";
            content= "comment1";
            UserDto userDto = UserDto.builder()
                    .email(email)
                    .username("test").build();
            user = Users.of(userDto);
            PostDto postDto = PostDto.builder()
                    .title("test t")
                    .content("test c").build();
            post = Posts.of(postDto);
            given(usersRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(postsRepository.findById(1L)).willReturn(Optional.of(post));
        }

        @Nested
        @DisplayName("When we have a dto and a email")
        class CreateProperty{
            private CommentsRequestDto requestDto;

            @BeforeEach
            void setup(){
                requestDto = CommentsRequestDto.builder()
                        .postId(1L)
                        .content(content).build();
            }

            @Test
            @DisplayName("Then you can create a comment")
            void createComment(){
                commentsService.createComment(requestDto, email);
                verify(usersRepository, times(1)).findByEmail(email);
                verify(postsRepository, times(1)).findById(1L);
                verify(commentsRepository, times(1)).findById(null);
                verify(commentsRepository, times(1)).save(argThat(
                        comments -> comments.getPost() == post && comments.getContent() == content &&
                                comments.getCreatedBy() == user
                ));
            }
        }
    }

}