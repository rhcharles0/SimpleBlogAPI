package dev.charles.SimpleService.users.repository;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.charles.SimpleService.users.domain.Users;
import dev.charles.SimpleService.users.dto.QUserDto;
import dev.charles.SimpleService.users.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.charles.SimpleService.users.domain.QUsers.users;

public class CustomizedUsersRepositoryImpl extends QuerydslRepositorySupport implements CustomizedUsersRepository{
    private final JPAQueryFactory queryFactory;

    public CustomizedUsersRepositoryImpl(JPAQueryFactory queryFactory) {
        super(Users.class);
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<UserDto> findAllByKeyword(Boolean isSearchMode, String keyword, Pageable pageable) {
        JPAQuery<Long> idQuery = queryFactory
                .select(users.id)
                .from(users)
                .where(users.username.likeIgnoreCase("%"+keyword + "%"))
                .orderBy(users.id.desc());
        JPQLQuery<Long> paginationId = querydsl().applyPagination(pageable, idQuery);
        List<Long> ids = paginationId.fetch();
        if(ids.isEmpty()){
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(0,10), 0);
        }

        JPAQuery<UserDto> query =queryFactory
                .select(new QUserDto(users.email, users.username))
                .from(users)
                .where(
                        users.id.in(ids)
                )
                .orderBy(users.id.desc());
        List<UserDto> contents = query.fetch();

        if(isSearchMode) {
            int fixedPageCount = 10 * pageable.getPageSize();
            return new PageImpl<>(contents, pageable, fixedPageCount);
        }
        Long totalCount = paginationId.fetchCount();
        return new PageImpl<>(contents, pageable, totalCount);
    }

    private Querydsl querydsl() {
        return Objects.requireNonNull(getQuerydsl());
    }

}
