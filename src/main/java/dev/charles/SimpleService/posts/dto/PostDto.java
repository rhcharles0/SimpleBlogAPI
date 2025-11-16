package dev.charles.SimpleService.posts.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;

@Getter
@NoArgsConstructor
public class PostDto {
    private String title;
    private String content;

    @PersistenceCreator
    @Builder
    public PostDto(String title, String content){
        this.content = content;
        this.title = title;
    }
}
