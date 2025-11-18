package dev.charles.SimpleService.comments.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.charles.SimpleService.comments.dto.CommentsResponseDto;
import dev.charles.SimpleService.users.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static dev.charles.SimpleService.comments.domain.QComments.comments;
import static dev.charles.SimpleService.users.domain.QUsers.users;

@RequiredArgsConstructor
public class CustomizedCommentsRepositoryImpl implements CustomizedCommentsRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentsResponseDto> findAllParentsByPostId(Long postId, Pageable pageable) {
        return queryFactory
                .select(Projections.fields(CommentsResponseDto.class
                        ,comments.content
                        ,comments.createdAt
                        ,comments.updatedAt
                        ,Projections.fields(UserDto.class,
                                comments.createdBy.username,
                                comments.createdBy.email).as("createdBy"))
                )
                .from(comments)
                .join(comments.createdBy, users)
                .where(
                        comments.post.id.eq(postId),
                        comments.parentComment.isNull()
                )
                .orderBy(comments.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset((long) pageable.getPageSize() * pageable.getPageNumber())
                .fetch();
    }

    @Override
    public List<CommentsResponseDto> findAllChildrenByParentId(Long parentId, Pageable pageable) {
        return queryFactory
                .select(Projections.fields(CommentsResponseDto.class
                        ,comments.content
                        ,comments.createdAt
                        ,comments.updatedAt
                        ,Projections.fields(UserDto.class,
                                comments.createdBy.username,
                                comments.createdBy.email).as("createdBy"))
                )
                .from(comments)
                .join(comments.createdBy, users)
                .where(
                        comments.parentComment.id.eq(parentId)
                )
                .orderBy(comments.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset((long) pageable.getPageSize() * pageable.getPageNumber())
                .fetch();
    }

    @Override
    public Long countParentsByPostId(Long postId) {
        return queryFactory.select(comments.count())
                .from(comments)
                .where(
                        comments.post.id.eq(postId),
                        comments.parentComment.isNull()
                ).fetchOne();
    }

    @Override
    public Long countChildrenByParentId(Long parentId) {
        return queryFactory.select(comments.count())
                .from(comments)
                .where(
                        comments.parentComment.id.eq(parentId)
                ).fetchOne();
    }
}
