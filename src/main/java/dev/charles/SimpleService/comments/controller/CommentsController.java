package dev.charles.SimpleService.comments.controller;

import dev.charles.SimpleService.comments.dto.CommentsRequestDto;
import dev.charles.SimpleService.comments.dto.CommentsResponseDto;
import dev.charles.SimpleService.comments.service.CommentsService;
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
@RequestMapping(path = "/api/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class CommentsController {
    final private CommentsService commentsService;

    @PostMapping
    public ResponseEntity<?> createComment(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestBody CommentsRequestDto requestDto) {
        String email = principal.getAttribute("email");
        commentsService.createComment(requestDto, email);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    @PatchMapping
    public ResponseEntity<?> updateComment(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestParam(value = "commentId") Long commentId,
            @RequestParam(value = "updateComment") String updateComment) {
        String email = principal.getAttribute("email");
        commentsService.updateComment(commentId,updateComment,email);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteComment(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestParam(value = "commentId") Long commentId) {
        String email = principal.getAttribute("email");
        commentsService.deleteComment(commentId,email);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @GetMapping("/paged/post")
    @PatchMapping
    public ResponseEntity<?> getPostComments(
            @RequestParam(value = "postId") Long postId,
            @RequestParam(value = "pageNumber") Integer pageNumber,
            @RequestParam(value = "total", required = false) Long total) {
        Page<CommentsResponseDto> result =commentsService.getCommentsByPostId(postId,pageNumber,total);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/paged/reply")
    @PatchMapping
    public ResponseEntity<?> getReplies(
            @RequestParam(value = "parentId") Long parentId,
            @RequestParam(value = "pageNumber") Integer pageNumber,
            @RequestParam(value = "total", required = false) Long total) {
        Page<CommentsResponseDto> result = commentsService.getRepliesByParentId(parentId,pageNumber,total);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
