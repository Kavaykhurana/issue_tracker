package com.issuetracker.service;

import com.issuetracker.entity.enums.IssueStatus;
import com.issuetracker.exception.InvalidStatusTransitionException;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.issuetracker.entity.enums.IssueStatus.*;

@Service
public class StatusTransitionService {

    private static final Map<IssueStatus, List<IssueStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<IssueStatus, List<IssueStatus>> map = new EnumMap<>(IssueStatus.class);
        map.put(BACKLOG,     List.of(TODO));
        map.put(TODO,        List.of(IN_PROGRESS, BACKLOG));
        map.put(IN_PROGRESS, List.of(IN_REVIEW,   BACKLOG));
        map.put(IN_REVIEW,   List.of(DONE,         IN_PROGRESS));
        map.put(DONE,        List.of(IN_PROGRESS));
        map.put(CANCELLED,   List.of(BACKLOG));
        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public void validate(IssueStatus current, IssueStatus requested) {
        List<IssueStatus> allowed =
                ALLOWED_TRANSITIONS.getOrDefault(current, List.of());

        if (!allowed.contains(requested)) {
            throw new InvalidStatusTransitionException(
                    current.name(), requested.name());
        }
    }

    public List<IssueStatus> getAllowedTransitions(IssueStatus current) {
        return ALLOWED_TRANSITIONS.getOrDefault(current, List.of());
    }
}
