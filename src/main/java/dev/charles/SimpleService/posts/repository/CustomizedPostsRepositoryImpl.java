package dev.charles.SimpleService.posts.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.charles.SimpleService.posts.domain.Posts;
import dev.charles.SimpleService.posts.dto.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dev.charles.SimpleService.posts.domain.QPosts.posts;
import static dev.charles.SimpleService.users.domain.QUsers.users;


public class CustomizedPostsRepositoryImpl extends QuerydslRepositorySupport implements CustomizedPostsRepository {
    private final JPAQueryFactory queryFactory;
    public CustomizedPostsRepositoryImpl(JPAQueryFactory queryFactory) {
        super(Posts.class);
        this.queryFactory = queryFactory;
    }

    private BooleanExpression likeIgnoreCase(String keyword) {
        if(Optional.ofNullable(keyword).isEmpty()) return null;
        return posts.title.likeIgnoreCase("%" + keyword + "%");
    }

    @Override
    public Page<PostDto> findAllByKeyword(boolean isSearchMode, String keyword, Pageable pageable) {

        JPAQuery<Long> idQuery = queryFactory
                .select(posts.id)
                .from(posts)
                .where(
                        likeIgnoreCase(keyword)
                )
                .orderBy(posts.id.desc());
        JPQLQuery<Long> paginationId = querydsl().applyPagination(pageable, idQuery);
        List<Long> ids = paginationId.fetch();

        if(ids.isEmpty()){
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(0,10), 0);
        }

        JPAQuery<PostDto> query = queryFactory.select(Projections.fields(PostDto.class,
                                        posts.title,
                                        posts.content))
                                .from(posts)
                                .where(
                                        posts.id.in(ids)
                                )
                                .orderBy(posts.id.desc());

        List<PostDto> content = query.fetch();
        if(isSearchMode) {
            int fixedPageCount = 10 * pageable.getPageSize();
            return new PageImpl<>(content, pageable, fixedPageCount);
        }

        long totalCount = paginationId.fetchCount();
        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public Page<PostDto> findAllByKeywordAndEmail(boolean isSearchMode, String keyword, String email, Pageable pageable) {
        JPAQuery<Long> idQuery = queryFactory
                .select(posts.id)
                .from(posts)
                .join(posts.createdBy, users)
                .where(
                        likeIgnoreCase(keyword),
                        users.email.eq(email)
                )
                .orderBy(posts.id.desc());
        JPQLQuery<Long> paginationId = querydsl().applyPagination(pageable, idQuery);
        List<Long> ids = paginationId.fetch();

        if(ids.isEmpty()){
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(0,10), 0);
        }
        JPAQuery<PostDto> query = queryFactory
                .select(Projections.fields(PostDto.class,
                        posts.title,
                        posts.content))
                .from(posts)
                .join(posts.createdBy, users)
                .where(
                       posts.id.in(ids)
                )
                .orderBy(posts.id.desc());

        List<PostDto> content = query.fetch();

        if(isSearchMode) {
            int fixedPageCount = 10 * pageable.getPageSize();
            return new PageImpl<>(content, pageable, fixedPageCount);
        }
        long totalCount = paginationId.fetchCount();
        return new PageImpl<>(content, pageable, totalCount);

    }

    private Querydsl querydsl() {
        return Objects.requireNonNull(getQuerydsl());
    }


}
