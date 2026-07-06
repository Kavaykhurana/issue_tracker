package com.issuetracker.service;

import com.issuetracker.dto.request.*;
import com.issuetracker.dto.response.*;
import com.issuetracker.entity.*;
import com.issuetracker.entity.enums.*;
import com.issuetracker.exception.*;
import com.issuetracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository       projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository          userRepository;
    private final IssueRepository         issueRepository;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, User currentUser) {
        if (projectRepository.existsByKey(request.getKey().toUpperCase())) {
            throw new DuplicateResourceException(
                    "Project key already in use: " + request.getKey());
        }

        Project project = Project.builder()
                .name(request.getName())
                .key(request.getKey().toUpperCase())
                .description(request.getDescription())
                .owner(currentUser)
                .build();

        projectRepository.save(project);

        ProjectMember ownerMembership = ProjectMember.builder()
                .project(project)
                .user(currentUser)
                .role(MemberRole.ADMIN)
                .build();
        memberRepository.save(ownerMembership);

        return toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjectsForUser(User currentUser) {
        return projectRepository.findAllByUserId(currentUser.getId()).stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, User currentUser) {
        Project project = findProjectOrThrow(projectId);
        assertIsMember(project, currentUser);
        return toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId,
                                         UpdateProjectRequest request,
                                         User currentUser) {
        Project project = findProjectOrThrow(projectId);
        assertIsAdminOrOwner(project, currentUser);

        if (request.getName()        != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());

        return toProjectResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long projectId, User currentUser) {
        Project project = findProjectOrThrow(projectId);
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the project owner can delete this project");
        }
        projectRepository.delete(project);
    }

    @Transactional
    public MemberResponse addMember(Long projectId,
                                    AddMemberRequest request,
                                    User currentUser) {
        Project project = findProjectOrThrow(projectId);
        assertIsAdminOrOwner(project, currentUser);

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + request.getUserId()));

        if (memberRepository.existsByProjectIdAndUserId(projectId, targetUser.getId())) {
            throw new DuplicateResourceException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(targetUser)
                .role(request.getRole())
                .build();

        return toMemberResponse(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(Long projectId, User currentUser) {
        Project project = findProjectOrThrow(projectId);
        assertIsMember(project, currentUser);

        return memberRepository.findAllByProjectId(projectId).stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberResponse updateMemberRole(Long projectId,
                                           Long userId,
                                           UpdateMemberRoleRequest request,
                                           User currentUser) {
        Project project = findProjectOrThrow(projectId);
        assertIsAdminOrOwner(project, currentUser);

        ProjectMember member = memberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Member not found in project"));

        if (project.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot change the owner's role");
        }

        member.setRole(request.getRole());
        return toMemberResponse(memberRepository.save(member));
    }

    @Transactional
    public void removeMember(Long projectId, Long userId, User currentUser) {
        Project project = findProjectOrThrow(projectId);
        assertIsAdminOrOwner(project, currentUser);

        if (project.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot remove the project owner");
        }

        ProjectMember member = memberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Member not found in project"));

        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public ProjectStatsResponse getStats(Long projectId, User currentUser) {
        Project project = findProjectOrThrow(projectId);
        assertIsMember(project, currentUser);

        var byStatus   = toMap(issueRepository.countByStatusForProject(projectId));
        var byPriority = toMap(issueRepository.countByPriorityForProject(projectId));
        var byAssignee = toMap(issueRepository.countByAssigneeForProject(projectId));

        return ProjectStatsResponse.builder()
                .projectId(projectId)
                .totalIssues(issueRepository.countByProjectId(projectId))
                .byStatus(byStatus)
                .byPriority(byPriority)
                .byAssignee(byAssignee)
                .build();
    }

    public Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));
    }

    public void assertIsMember(Project project, User user) {
        boolean isOwner  = project.getOwner().getId().equals(user.getId());
        boolean isMember = memberRepository.existsByProjectIdAndUserId(
                project.getId(), user.getId());
        if (!isOwner && !isMember) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    public void assertIsAdminOrOwner(Project project, User user) {
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isAdmin = memberRepository.existsByProjectIdAndUserIdAndRole(
                project.getId(), user.getId(), MemberRole.ADMIN);
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "You need admin or owner access for this action");
        }
    }

    private ProjectResponse toProjectResponse(Project project) {
        long total = issueRepository.countByProjectId(project.getId());
        long open  = total
                - issueRepository.countByProjectIdAndStatus(project.getId(), IssueStatus.DONE)
                - issueRepository.countByProjectIdAndStatus(project.getId(), IssueStatus.CANCELLED);

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .key(project.getKey())
                .description(project.getDescription())
                .owner(IssueMapper.toUserResponse(project.getOwner()))
                .createdAt(project.getCreatedAt())
                .memberCount(memberRepository.findAllByProjectId(project.getId()).size())
                .totalIssues(total)
                .openIssues(open)
                .build();
    }

    private MemberResponse toMemberResponse(ProjectMember member) {
        return MemberResponse.builder()
                .id(member.getId())
                .user(IssueMapper.toUserResponse(member.getUser()))
                .role(member.getRole())
                .build();
    }

    private java.util.Map<String, Long> toMap(List<Object[]> rows) {
        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put(row[0].toString(), (Long) row[1]);
        }
        return result;
    }
}
