package dev.charles.SimpleService.users.domain;

import dev.charles.SimpleService.posts.domain.Posts;
import dev.charles.SimpleService.users.dto.UserDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Users extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "createdBy" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Posts> posts = new ArrayList<>();

    @Version
    private Long version;

    public static Users of(UserDto userDto){
        return new Users(userDto.getUsername(), userDto.getEmail());
    }

    public Users(String username, String email){
        this.username = username;
        this.email = email;
    }

    public void update(UserDto userDto){
        this.username = userDto.getUsername();
        this.email = userDto.getEmail();
    }

}
