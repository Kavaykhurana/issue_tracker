package com.issuetracker.repository;

import com.issuetracker.entity.IssueLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueLabelRepository extends JpaRepository<IssueLabel, Long> {
    List<IssueLabel> findAllByProjectId(Long projectId);
}
