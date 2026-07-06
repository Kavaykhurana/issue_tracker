package com.issuetracker.repository;

import com.issuetracker.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByKey(String key);

    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN p.members m
        WHERE p.owner.id = :userId
           OR m.user.id  = :userId
        """)
    List<Project> findAllByUserId(@Param("userId") Long userId);
}
