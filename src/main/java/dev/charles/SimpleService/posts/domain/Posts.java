package dev.charles.SimpleService.posts.domain;

import dev.charles.SimpleService.posts.dto.PostDto;
import dev.charles.SimpleService.users.domain.BaseEntity;
import dev.charles.SimpleService.users.domain.Users;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Posts extends BaseEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Users createdBy;

    @Version
    private Long version;

    public Posts(String title, String content){
        this.title = title;
        this.content = content;
    }

    public static Posts of(PostDto postDto){
        return new Posts(postDto.getTitle(), postDto.getContent());
    }
    public void update(PostDto postDto){
        this.content = postDto.getContent();
        this.title = postDto.getTitle();
    }

    public void setUser(Users user){
        this.createdBy = user;
    }


}
