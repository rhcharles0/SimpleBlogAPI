package dev.charles.SimpleService.comments;

import dev.charles.SimpleService.AbstractIntegrationTest;
import dev.charles.SimpleService.comments.dto.CommentsRequestDto;
import dev.charles.SimpleService.comments.service.CommentsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentsControllerTest extends AbstractIntegrationTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    CommentsService commentsService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Nested
    @DisplayName("Given we are authorized users and have posts")
    class AuthorizedTest{
        String email;
        @BeforeEach
        void setup(){
            email = "sample@email.com";
        }

        @Nested
        @DisplayName("When we have a CommentsRequestDto")
        class CreateProperty{
            private String text = "comment";
            private CommentsRequestDto requestDto = CommentsRequestDto.builder()
                    .postId(1L)
                    .parentId(null)
                    .content(text).build();
            @Test
            @DisplayName("Then you can create a comment")
            void createComment() throws Exception {
                mockMvc.perform(post("/api/comments").with(opaqueToken()
                        .attributes(attrs -> attrs.put("email",email)))
                                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)
                                ))
                        .andExpect(status().isCreated())
                        .andExpect(content().string(""));
                verify(commentsService, times(1)).createComment(argThat(dto->
                        dto.getContent().equals(text) && dto.getPostId().equals(1L) && dto.getParentId() == null), eq(email));
            }
        }
        @Nested
        @DisplayName("When we have a commentId")
        class DeleteProperty{
            private Long commentId = 1L;
            @Test
            @DisplayName("Then you can delete a comment with commentId")
            void deleteComment() throws Exception {
                mockMvc.perform(delete("/api/comments").with(opaqueToken()
                                        .attributes(attrs -> attrs.put("email",email)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("commentId", "1"))
                        .andExpect(status().isNoContent())
                        .andExpect(content().string(""));

                verify(commentsService, times(1)).deleteComment(commentId, email);
            }

        }

        @Nested
        @DisplayName("When we have a commentId and a new text")
        class UpdateProperty{
            private Long commentId = 1L;
            private String updateComment = "new comment";
            @Test
            @DisplayName("Then you can update a comment with new text")
            void updateComment() throws Exception {
                mockMvc.perform(patch("/api/comments").with(opaqueToken()
                                        .attributes(attrs -> attrs.put("email",email)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("commentId", "1")
                                .param("updateComment", updateComment ))
                        .andExpect(status().isNoContent())
                        .andExpect(content().string(""));

                verify(commentsService, times(1)).updateComment(commentId, updateComment, email);
            }
        }

    }

    @Nested
    @DisplayName("Given we are anonymous")
    class NotAuthorizedTest{

        @Nested
        @DisplayName("When we have a post and a page number")
        class PostCommentsProperty{
            private Integer pageNumber = 0;
            private Long postId = 1L;
            @Test
            @DisplayName("Then you can get a pagination of comments by a pagenumber")
            void getPostComments() throws Exception{
                mockMvc.perform(get("/api/comments/paged/post")
                        .param("postId", "1")
                        .param("pageNumber", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());

                verify(commentsService, times(1)).getCommentsByPostId(postId, pageNumber, null);
            }

        }

        @Nested
        @DisplayName("When we have a parentId and a page number")
        class ReplyProperty{
            private Integer pageNumber = 0;
            private Long parentId = 1L;
            @Test
            @DisplayName("Then you can get a pagination of comments by a pagenumber")
            void getReplies() throws Exception{
                mockMvc.perform(get("/api/comments/paged/reply")
                                .param("parentId", "1")
                                .param("pageNumber", "0")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());

                verify(commentsService, times(1)).getRepliesByParentId(parentId, pageNumber, null);
            }

        }
    }



}