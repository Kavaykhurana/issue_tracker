package com.issuetracker.service;

import com.issuetracker.dto.request.*;
import com.issuetracker.dto.response.IssueResponse;
import com.issuetracker.entity.*;
import com.issuetracker.entity.enums.*;
import com.issuetracker.exception.*;
import com.issuetracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository         issueRepository;
    private final ProjectService           projectService;
    private final UserRepository           userRepository;
    private final StatusTransitionService  transitionService;

    @Transactional
    public IssueResponse createIssue(Long projectId,
                                     CreateIssueRequest request,
                                     User currentUser) {
        Project project = projectService.findProjectOrThrow(projectId);
        projectService.assertIsMember(project, currentUser);

        Issue.IssueBuilder builder = Issue.builder()
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .reporter(currentUser)
                .status(request.getStatus()   != null ? request.getStatus()   : IssueStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : IssuePriority.MEDIUM)
                .type(request.getType()       != null ? request.getType()      : IssueType.TASK)
                .dueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Assignee not found: " + request.getAssigneeId()));
            builder.assignee(assignee);
        }

        if (request.getParentIssueId() != null) {
            Issue parent = findIssueOrThrow(request.getParentIssueId());
            builder.parentIssue(parent);
        }

        Issue saved = issueRepository.save(builder.build());
        return IssueMapper.toSummaryResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<IssueResponse> listIssues(Long projectId,
                                          IssueStatus status,
                                          IssuePriority priority,
                                          IssueType type,
                                          Long assigneeId,
                                          Pageable pageable,
                                          User currentUser) {
        Project project = projectService.findProjectOrThrow(projectId);
        projectService.assertIsMember(project, currentUser);

        Specification<Issue> spec = Specification
                .where(hasProject(projectId))
                .and(hasStatus(status))
                .and(hasPriority(priority))
                .and(hasType(type))
                .and(hasAssignee(assigneeId));

        return issueRepository.findAll(spec, pageable)
                .map(IssueMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public IssueResponse getIssueById(Long issueId, User currentUser) {
        Issue issue = findIssueOrThrow(issueId);
        projectService.assertIsMember(issue.getProject(), currentUser);
        return IssueMapper.toDetailResponse(issue);
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> getIssuesAssignedToMe(User currentUser) {
        return issueRepository.findAllByAssigneeId(currentUser.getId()).stream()
                .map(IssueMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueResponse updateIssue(Long issueId,
                                     UpdateIssueRequest request,
                                     User currentUser) {
        Issue issue = findIssueOrThrow(issueId);
        projectService.assertIsMember(issue.getProject(), currentUser);

        if (request.getTitle()    != null) issue.setTitle(request.getTitle());
        if (request.getDescription() != null) issue.setDescription(request.getDescription());
        if (request.getPriority() != null) issue.setPriority(request.getPriority());
        if (request.getType()     != null) issue.setType(request.getType());
        if (request.getDueDate()  != null) issue.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            if (request.getAssigneeId() == -1L) {
                issue.setAssignee(null);
            } else {
                User assignee = userRepository.findById(request.getAssigneeId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Assignee not found: " + request.getAssigneeId()));
                issue.setAssignee(assignee);
            }
        }

        return IssueMapper.toSummaryResponse(issueRepository.save(issue));
    }

    @Transactional
    public IssueResponse transitionStatus(Long issueId,
                                          StatusTransitionRequest request,
                                          User currentUser) {
        Issue issue = findIssueOrThrow(issueId);
        projectService.assertIsMember(issue.getProject(), currentUser);

        transitionService.validate(issue.getStatus(), request.getNewStatus());
        issue.setStatus(request.getNewStatus());

        return IssueMapper.toSummaryResponse(issueRepository.save(issue));
    }

    @Transactional
    public void deleteIssue(Long issueId, User currentUser) {
        Issue issue = findIssueOrThrow(issueId);
        projectService.assertIsAdminOrOwner(issue.getProject(), currentUser);
        issueRepository.delete(issue);
    }

    @Transactional
    public IssueResponse addLabel(Long issueId, IssueLabel label, User currentUser) {
        Issue issue = findIssueOrThrow(issueId);
        projectService.assertIsMember(issue.getProject(), currentUser);
        issue.getLabels().add(label);
        return IssueMapper.toSummaryResponse(issueRepository.save(issue));
    }

    @Transactional
    public IssueResponse removeLabel(Long issueId, IssueLabel label, User currentUser) {
        Issue issue = findIssueOrThrow(issueId);
        projectService.assertIsMember(issue.getProject(), currentUser);
        issue.getLabels().remove(label);
        return IssueMapper.toSummaryResponse(issueRepository.save(issue));
    }

    public Issue findIssueOrThrow(Long issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Issue not found: " + issueId));
    }

    private Specification<Issue> hasProject(Long projectId) {
        return (root, query, cb) ->
                cb.equal(root.get("project").get("id"), projectId);
    }

    private Specification<Issue> hasStatus(IssueStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction()
                               : cb.equal(root.get("status"), status);
    }

    private Specification<Issue> hasPriority(IssuePriority priority) {
        return (root, query, cb) ->
                priority == null ? cb.conjunction()
                                 : cb.equal(root.get("priority"), priority);
    }

    private Specification<Issue> hasType(IssueType type) {
        return (root, query, cb) ->
                type == null ? cb.conjunction()
                             : cb.equal(root.get("type"), type);
    }

    private Specification<Issue> hasAssignee(Long assigneeId) {
        return (root, query, cb) ->
                assigneeId == null ? cb.conjunction()
                                   : cb.equal(root.get("assignee").get("id"), assigneeId);
    }
}
