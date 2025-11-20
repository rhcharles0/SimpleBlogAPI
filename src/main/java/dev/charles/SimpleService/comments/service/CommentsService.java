package dev.charles.SimpleService.comments.service;

import dev.charles.SimpleService.comments.domain.Comments;
import dev.charles.SimpleService.comments.dto.CommentsRequestDto;
import dev.charles.SimpleService.comments.dto.CommentsResponseDto;
import dev.charles.SimpleService.comments.repository.CommentsRepository;
import dev.charles.SimpleService.errors.exception.NotAuthorizedException;
import dev.charles.SimpleService.errors.exception.NotFoundResourceException;
import dev.charles.SimpleService.posts.domain.Posts;
import dev.charles.SimpleService.posts.repository.PostsRepository;
import dev.charles.SimpleService.users.domain.Users;
import dev.charles.SimpleService.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final PostsRepository postsRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public void createComment(final CommentsRequestDto requestDto, final String email) {
        Posts post =postsRepository.findById(requestDto.getPostId())
                .orElseThrow(()-> new NotFoundResourceException("Post not found by id: "+ requestDto.getPostId()));
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundResourceException("User not found by email: " + email));
        Comments parentComment = commentsRepository.findById(requestDto.getParentId()).orElseGet(()-> null);
        Comments newComment = Comments.builder()
                .content(requestDto.getContent())
                .user(user)
                .post(post)
                .parentComment(parentComment)
                .build();
        commentsRepository.save(newComment);
    }

    public Page<CommentsResponseDto> getCommentsByPostId(final Long postId, final Integer pageNumber, final Long total) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<CommentsResponseDto> commentsList =  commentsRepository.findAllParentsByPostId(postId,pageable);
        long totalCount = Optional.ofNullable(total).orElseGet(()->commentsRepository.countParentsByPostId(postId));
        return new PageImpl<>(commentsList, pageable, totalCount);
    }

    public Page<CommentsResponseDto> getRepliesByParentId(Long parentId, final Integer pageNumber, final Long total) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<CommentsResponseDto> commentsList =  commentsRepository.findAllChildrenByParentId(parentId,pageable);
        long totalCount = Optional.ofNullable(total).orElseGet(()->commentsRepository.countChildrenByParentId(parentId));
        return new PageImpl<>(commentsList, pageable, totalCount);
    }

    @Transactional
    public void updateComment(final Long commentId, final String updateComment, final String email) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundResourceException("Comment not found by id: "+commentId));
        hasAuthorized(comment.getCreatedBy(), email);
        comment.update(updateComment);
    }

    @Transactional
    public void deleteComment(final Long commentId, final String email) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundResourceException("Comment not found by id: "+commentId));
        hasAuthorized(comment.getCreatedBy(), email);
        commentsRepository.delete(comment);
    }

    private void hasAuthorized(final Users user, final String email){
        if(!user.getEmail().equals(email)) {
            throw new NotAuthorizedException("You're not writer on this comment.");
        }
    }

}
