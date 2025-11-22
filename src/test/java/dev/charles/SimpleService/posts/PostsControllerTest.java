package dev.charles.SimpleService.posts;

import dev.charles.SimpleService.AbstractIntegrationTest;
import dev.charles.SimpleService.posts.dto.PostDto;
import dev.charles.SimpleService.posts.service.PostsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PostsControllerTest extends AbstractIntegrationTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean
    private PostsService postsService;

    private PostDto postDto = PostDto.builder()
            .title("test1")
            .content("content").build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @Test
    void save() throws Exception {
        // Given
        String email = "sample@email.com";
        // When & Then
        mockMvc.perform(post("/api/posts").with(opaqueToken()
                                .attributes(attrs ->
                                    attrs.put("email", email)
                                ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(postDto))
                )
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @Test
    void update() throws Exception {
        //given
        PostDto updateDto = PostDto.builder()
                .title("fixed title")
                .content("content2")
                .build();

        //when, then
        mockMvc.perform(put("/api/posts")
                .with(opaqueToken()
                .attributes(attrs ->
                        attrs.put("email", "sample@email.com")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .param("id","1")
                .content(objectMapper.writeValueAsString(updateDto))
        ).andDo(print())
                .andExpect(status().isNoContent());
        verify(postsService, times(1)).updatePost(eq(1L), any(PostDto.class));
    }

    @Test
    void deletePost() throws Exception{
        //when, then
        mockMvc.perform(delete("/api/posts")
                        .with(opaqueToken()
                                .attributes(attrs ->
                                        attrs.put("email", "sample@email.com")
                                ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("id","1")
                )
                .andExpect(status().isNoContent());
        verify(postsService, times(1)).deletePost(1L);

    }
    @Test
    void getPost() throws Exception {
        //given
        given(postsService.getPostById(1L)).willReturn(postDto);
        //when, then
        mockMvc.perform(get("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("id","1")
                )
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(postDto)));
        verify(postsService, times(1)).getPostById(1L);
    }

    @Test
    void getPostsByKeyword() throws Exception {
        //given
        List<PostDto> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PostDto tempDto = PostDto.builder()
                    .title("hi"+i)
                    .content("content")
                    .build();
            list.add(tempDto);
        }
        Page<PostDto> result = new PageImpl<>(list, PageRequest.of(0,5),5);
        given(postsService.getAllPosts(eq(false), any(), any())).willReturn(result);
        //when, then
        mockMvc.perform(get("/api/posts/paged")
                .contentType(MediaType.APPLICATION_JSON)
                        .param("isSearchMode", "false")
                .param("keyword", "hi")
                .param("pageNumber", "1")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    void getPostsByKeywordAndEmail() throws Exception{
        //given
        List<PostDto> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PostDto tempDto = PostDto.builder()
                    .title("hi"+i)
                    .content("content")
                    .build();
            list.add(tempDto);
        }
        Page<PostDto> result = new PageImpl<>(list, PageRequest.of(0,5),5);
        given(postsService.getAllPostsByUser(eq(false), any(), any(), any())).willReturn(result);
        //when, then
        mockMvc.perform(get("/api/posts/paged/user")
                        .param("isSearchMode", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("keyword", "hi")
                        .param("pageNumber", "1")
                        .param("email", "email@email.com")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }
}