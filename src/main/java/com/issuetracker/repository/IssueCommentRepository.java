package com.issuetracker.repository;

import com.issuetracker.entity.IssueComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {
    List<IssueComment> findAllByIssueIdOrderByCreatedAtAsc(Long issueId);
}
