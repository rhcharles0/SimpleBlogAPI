package dev.charles.SimpleService.posts.controller;

import dev.charles.SimpleService.posts.dto.PostDto;
import dev.charles.SimpleService.posts.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/posts", consumes = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class PostsController {
    final private PostsService postsService;

    @PostMapping
    public ResponseEntity<?> save(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal, @RequestBody PostDto postDto){
        postsService.createPost(principal.getAttribute("email"), postDto );
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestParam(value = "id") Long id, @RequestBody PostDto postDto){
        postsService.updatePost(id, postDto);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam(value = "id") Long id){
        postsService.deletePost(id);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<?> getPost(@RequestParam(value = "id") Long id){
        PostDto postDto = postsService.getPostById(id);
        return new ResponseEntity<>(postDto, HttpStatus.OK);
    }

    @GetMapping(path = "/paged")
    public ResponseEntity<?> getPostsByKeyword(@RequestParam(value = "keyword") String keyword,
                                      @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                      @RequestParam(value = "total", required= false) Long total ){
        Page<PostDto> result = postsService.getAllPosts(keyword, pageNumber, total);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path = "/paged/user")
    public ResponseEntity<?> getPostsByKeywordAndEmail(@RequestParam(value = "keyword") String keyword,
                                               @RequestParam(value = "email") String email,
                                               @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                               @RequestParam(value = "total", required= false) Long total ){
        Page<PostDto> result = postsService.getAllPostsbyUser(email,keyword, pageNumber, total);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }




}
