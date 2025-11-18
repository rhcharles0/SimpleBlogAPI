package dev.charles.SimpleService.comments.repository;

import dev.charles.SimpleService.comments.dto.CommentsResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomizedCommentsRepository {
    List<CommentsResponseDto> findAllParentsByPostId(Long postId, Pageable pageable);
    List<CommentsResponseDto> findAllChildrenByParentId(Long parentId, Pageable pageable);
    Long countParentsByPostId(Long postId);
    Long countChildrenByParentId(Long postId);
}
