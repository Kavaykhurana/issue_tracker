package com.issuetracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "issue_labels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 7)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}
