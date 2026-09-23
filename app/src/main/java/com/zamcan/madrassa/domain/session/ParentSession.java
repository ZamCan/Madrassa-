package com.zamcan.madrassa.domain.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ParentSession {

    public final String sessionId;
    public final String parentId;
    public final String madrassaId;

    private final List<String> linkedStudentIds;

    public ParentSession(
            String sessionId,
            String parentId,
            String madrassaId,
            List<String> linkedStudentIds
    ) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId is required");
        }

        if (parentId == null || parentId.trim().isEmpty()) {
            throw new IllegalArgumentException("parentId is required");
        }

        this.sessionId = sessionId;
        this.parentId = parentId;
        this.madrassaId = madrassaId;

        List<String> copy = new ArrayList<>();

        if (linkedStudentIds != null) {
            for (String id : linkedStudentIds) {
                if (id != null && !id.trim().isEmpty()) {
                    copy.add(id.trim());
                }
            }
        }

        this.linkedStudentIds =
                Collections.unmodifiableList(copy);
    }

    public boolean canAccessStudent(String studentId) {
        if (studentId == null) {
            return false;
        }

        return linkedStudentIds.contains(studentId.trim());
    }

    public List<String> getLinkedStudentIds() {
        return linkedStudentIds;
    }
}
