package dev.charles.SimpleService.comments.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.charles.SimpleService.comments.domain.Comments;
import dev.charles.SimpleService.comments.dto.CommentsResponseDto;
import dev.charles.SimpleService.comments.dto.QCommentsResponseDto;
import dev.charles.SimpleService.posts.domain.Posts;
import dev.charles.SimpleService.users.dto.QUserDto;
import dev.charles.SimpleService.users.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.charles.SimpleService.comments.domain.QComments.comments;
import static dev.charles.SimpleService.users.domain.QUsers.users;


public class CustomizedCommentsRepositoryImpl extends QuerydslRepositorySupport  implements CustomizedCommentsRepository {
    private final JPAQueryFactory queryFactory;

    public CustomizedCommentsRepositoryImpl(JPAQueryFactory queryFactory) {
        super(Comments.class);
        this.queryFactory = queryFactory;
    }


    @Override
    public Page<CommentsResponseDto> findAllParentsByPostId(Long postId, Pageable pageable) {
        JPAQuery<Long> idQuery = queryFactory
                .select(comments.id)
                .from(comments)
                .join(comments.createdBy, users)
                .where(
                        comments.post.id.eq(postId),
                        comments.parentComment.isNull()
                )
                .orderBy(comments.createdAt.desc());

        JPQLQuery<Long> paginationId = querydsl().applyPagination(pageable, idQuery);
        List<Long> ids = paginationId.fetch();

        if(ids.isEmpty()){
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        JPAQuery<CommentsResponseDto> query = queryFactory
                .select( new QCommentsResponseDto(comments.content, comments.createdAt, comments.updatedAt,
                                new QUserDto( comments.createdBy.username,
                                        comments.createdBy.email)))
                .from(comments)
                .where(comments.id.in(ids))
                .orderBy(comments.id.desc());
        List<CommentsResponseDto> contents = query.fetch();
        Long totalCount = paginationId.fetchCount();
        return new PageImpl<>(contents, pageable, totalCount);
    }

    @Override
    public Page<CommentsResponseDto> findAllChildrenByParentId(Long parentId, Pageable pageable) {
        JPAQuery<Long> idQuery = queryFactory
                .select(comments.id)
                .from(comments)
                .join(comments.createdBy, users)
                .where(
                        comments.parentComment.id.eq(parentId)
                )
                .orderBy(comments.createdAt.desc());

        JPQLQuery<Long> paginationId = querydsl().applyPagination(pageable, idQuery);
        List<Long> ids = paginationId.fetch();

        if(ids.isEmpty()){
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        JPAQuery<CommentsResponseDto> query = queryFactory
                .select( new QCommentsResponseDto(comments.content, comments.createdAt, comments.updatedAt,
                        new QUserDto( comments.createdBy.username,
                                comments.createdBy.email)))
                .from(comments)
                .join(comments.createdBy, users)
                .where(
                        comments.id.in(ids)
                )
                .orderBy(comments.createdAt.desc());
        List<CommentsResponseDto> contents = query.fetch();
        Long totalCount = paginationId.fetchCount();
        return new PageImpl<>(contents, pageable, totalCount);
    }

    private Querydsl querydsl() {
        return Objects.requireNonNull(getQuerydsl());
    }

}
