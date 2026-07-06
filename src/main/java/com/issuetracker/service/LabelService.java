package com.issuetracker.service;

import com.issuetracker.dto.request.CreateLabelRequest;
import com.issuetracker.dto.response.*;
import com.issuetracker.entity.*;
import com.issuetracker.repository.IssueLabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final IssueLabelRepository labelRepository;
    private final IssueService         issueService;
    private final ProjectService       projectService;

    @Transactional
    public LabelResponse createLabel(Long projectId,
                                     CreateLabelRequest request,
                                     User currentUser) {
        Project project = projectService.findProjectOrThrow(projectId);
        projectService.assertIsAdminOrOwner(project, currentUser);

        IssueLabel label = IssueLabel.builder()
                .name(request.getName())
                .color(request.getColor())
                .project(project)
                .build();

        return IssueMapper.toLabelResponse(labelRepository.save(label));
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> listLabels(Long projectId, User currentUser) {
        Project project = projectService.findProjectOrThrow(projectId);
        projectService.assertIsMember(project, currentUser);

        return labelRepository.findAllByProjectId(projectId).stream()
                .map(IssueMapper::toLabelResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueResponse addLabelToIssue(Long issueId, Long labelId, User currentUser) {
        IssueLabel label = findLabelOrThrow(labelId);
        return issueService.addLabel(issueId, label, currentUser);
    }

    @Transactional
    public IssueResponse removeLabelFromIssue(Long issueId, Long labelId, User currentUser) {
        IssueLabel label = findLabelOrThrow(labelId);
        return issueService.removeLabel(issueId, label, currentUser);
    }

    private IssueLabel findLabelOrThrow(Long labelId) {
        return labelRepository.findById(labelId)
                .orElseThrow(() -> new com.issuetracker.exception.ResourceNotFoundException(
                        "Label not found: " + labelId));
    }
}
