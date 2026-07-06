package com.issuetracker.service;

import com.issuetracker.dto.request.CreateCommentRequest;
import com.issuetracker.dto.response.CommentResponse;
import com.issuetracker.entity.*;
import com.issuetracker.exception.*;
import com.issuetracker.repository.IssueCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final IssueCommentRepository commentRepository;
    private final IssueService           issueService;
    private final ProjectService         projectService;

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long issueId, User currentUser) {
        Issue issue = issueService.findIssueOrThrow(issueId);
        projectService.assertIsMember(issue.getProject(), currentUser);

        return commentRepository.findAllByIssueIdOrderByCreatedAtAsc(issueId)
                .stream()
                .map(IssueMapper::toCommentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse addComment(Long issueId,
                                      CreateCommentRequest request,
                                      User currentUser) {
        Issue issue = issueService.findIssueOrThrow(issueId);
        projectService.assertIsMember(issue.getProject(), currentUser);

        IssueComment comment = IssueComment.builder()
                .issue(issue)
                .author(currentUser)
                .content(request.getContent())
                .build();

        return IssueMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse editComment(Long commentId,
                                       CreateCommentRequest request,
                                       User currentUser) {
        IssueComment comment = findCommentOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        return IssueMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        IssueComment comment = findCommentOrThrow(commentId);

        boolean isAuthor = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin  = projectService
                .findProjectOrThrow(comment.getIssue().getProject().getId())
                .getOwner().getId().equals(currentUser.getId());

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException(
                    "Only the comment author or project admin can delete this comment");
        }

        commentRepository.delete(comment);
    }

    private IssueComment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comment not found: " + commentId));
    }
}
