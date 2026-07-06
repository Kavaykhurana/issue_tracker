package com.issuetracker.repository;

import com.issuetracker.entity.Issue;
import com.issuetracker.entity.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueRepository
        extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {

    Page<Issue> findAllByProjectId(Long projectId, Pageable pageable);

    List<Issue> findAllByAssigneeId(Long assigneeId);

    @Query("SELECT i.status, COUNT(i) FROM Issue i WHERE i.project.id = :projectId GROUP BY i.status")
    List<Object[]> countByStatusForProject(@Param("projectId") Long projectId);

    @Query("SELECT i.priority, COUNT(i) FROM Issue i WHERE i.project.id = :projectId GROUP BY i.priority")
    List<Object[]> countByPriorityForProject(@Param("projectId") Long projectId);

    @Query("""
        SELECT i.assignee.username, COUNT(i)
        FROM Issue i
        WHERE i.project.id = :projectId
          AND i.assignee IS NOT NULL
        GROUP BY i.assignee.username
        """)
    List<Object[]> countByAssigneeForProject(@Param("projectId") Long projectId);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, IssueStatus status);
}
