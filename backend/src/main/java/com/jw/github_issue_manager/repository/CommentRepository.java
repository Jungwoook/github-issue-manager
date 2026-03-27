package com.jw.github_issue_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jw.github_issue_manager.domain.Comment;
import com.jw.github_issue_manager.domain.Issue;
import com.jw.github_issue_manager.domain.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author", "issue"})
    List<Comment> findByIssue(Issue issue);

    @EntityGraph(attributePaths = {"author", "issue"})
    Optional<Comment> findByIdAndIssueId(Long id, Long issueId);

    boolean existsByAuthor(User author);
}
