package dev.charles.SimpleService.comments.repository;

import dev.charles.SimpleService.comments.domain.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepository extends JpaRepository<Comments,Long>, CustomizedCommentsRepository{
}
