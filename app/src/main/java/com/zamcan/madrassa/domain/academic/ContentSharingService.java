package com.zamcan.madrassa.domain.academic;

import com.zamcan.madrassa.data.model.ContentShare;
import com.zamcan.madrassa.data.model.LearningMaterial;
import com.zamcan.madrassa.data.model.Lesson;
import com.zamcan.madrassa.data.model.Assignment;
import com.zamcan.madrassa.domain.repository.AssignmentStore;
import com.zamcan.madrassa.domain.repository.ContentShareStore;
import com.zamcan.madrassa.domain.repository.LearningMaterialStore;
import com.zamcan.madrassa.domain.repository.LessonStore;
import com.zamcan.madrassa.domain.common.OperationResult;

public final class ContentSharingService {

    private final ContentShareStore shares;
    private final LessonStore lessons;
    private final LearningMaterialStore materials;
    private final AssignmentStore assignments;

    public ContentSharingService(
            ContentShareStore shares,
            LessonStore lessons,
            LearningMaterialStore materials,
            AssignmentStore assignments
    ) {
        if (shares == null
                || lessons == null
                || materials == null
                || assignments == null) {
            throw new IllegalArgumentException(
                    "content sharing dependencies are required"
            );
        }

        this.shares = shares;
        this.lessons = lessons;
        this.materials = materials;
        this.assignments = assignments;
    }

    public OperationResult<ContentShare> share(
            ContentShare value
    ) {
        if (value == null
                || blank(value.id)
                || blank(value.madrassaId)
                || blank(value.contentType)
                || blank(value.contentId)
                || blank(value.scope)
                || invalidScopeTarget(value)) {
            return OperationResult.validationError(
                    "content_share_invalid",
                    "content share data is incomplete"
            );
        }

        String madrassaId = value.madrassaId.trim();
        String contentType = value.contentType.trim();
        String contentId = value.contentId.trim();

        if (!supportedContentType(contentType)) {
            return OperationResult.validationError(
                    "content_share_type_invalid",
                    "unsupported content type"
            );
        }

        /*
         * Resolve the canonical object first.
         *
         * Global content is allowed to be shared into a Madrassa
         * because this record grants access; it does not duplicate
         * the canonical content.
         */
        String contentOwner = canonicalMadrassa(
                contentType,
                contentId
        );

        if (contentOwner == null
                && !canonicalContentExists(contentType, contentId)) {
            return OperationResult.notFound(
                    "content_share_content_missing",
                    "canonical content was not found"
            );
        }

        /*
         * A Madrassa-owned canonical object can only be shared
         * inside that same Madrassa.
         */
        if (contentOwner != null
                && !madrassaId.equals(contentOwner)) {
            return OperationResult.forbidden(
                    "content_share_cross_madrassa",
                    "content belongs to another madrassa"
            );
        }

        value.id = value.id.trim();
        value.madrassaId = madrassaId;
        value.contentType = contentType;
        value.contentId = contentId;
        value.scope = value.scope.trim();

        if (blank(value.targetId)) {
            value.targetId = null;
        } else {
            value.targetId = value.targetId.trim();
        }

        if (blank(value.createdBy)) {
            value.createdBy = null;
        } else {
            value.createdBy = value.createdBy.trim();
        }

        value.active = true;

        long now = System.currentTimeMillis();

        if (value.createdAt <= 0) {
            value.createdAt = now;
        }

        value.updatedAt = now;

        return shares.save(value)
                ? OperationResult.success(value)
                : OperationResult.failed(
                        "content_share_save_failed",
                        "content share could not be saved"
                );
    }

    public OperationResult<ContentShare> revoke(
            String shareId,
            String madrassaId
    ) {
        if (blank(shareId) || blank(madrassaId)) {
            return OperationResult.validationError(
                    "content_share_revoke_invalid",
                    "shareId and madrassaId are required"
            );
        }

        ContentShare existing = shares.findById(
                shareId.trim()
        );

        if (existing == null) {
            return OperationResult.notFound(
                    "content_share_missing",
                    "content share was not found"
            );
        }

        if (!madrassaId.trim().equals(existing.madrassaId)) {
            return OperationResult.forbidden(
                    "content_share_revoke_cross_madrassa",
                    "share belongs to another madrassa"
            );
        }

        existing.active = false;
        existing.updatedAt = System.currentTimeMillis();

        return shares.update(existing)
                ? OperationResult.success(existing)
                : OperationResult.failed(
                        "content_share_revoke_failed",
                        "content share could not be revoked"
                );
    }

    private boolean canonicalContentExists(
            String type,
            String id
    ) {
        if (ContentShare.CONTENT_LESSON.equals(type)) {
            return lessons.findById(id) != null;
        }

        if (ContentShare.CONTENT_MATERIAL.equals(type)) {
            return materials.findById(id) != null;
        }

        if (ContentShare.CONTENT_ASSIGNMENT.equals(type)) {
            return assignments.findById(id) != null;
        }

        return false;
    }

    private String canonicalMadrassa(
            String type,
            String id
    ) {
        if (ContentShare.CONTENT_LESSON.equals(type)) {
            Lesson value = lessons.findById(id);
            return value == null ? null : value.madrassaId;
        }

        if (ContentShare.CONTENT_MATERIAL.equals(type)) {
            LearningMaterial value = materials.findById(id);
            return value == null ? null : value.madrassaId;
        }

        if (ContentShare.CONTENT_ASSIGNMENT.equals(type)) {
            Assignment value = assignments.findById(id);
            return value == null ? null : value.madrassaId;
        }

        return null;
    }

    private static boolean supportedContentType(
            String type
    ) {
        return ContentShare.CONTENT_LESSON.equals(type)
                || ContentShare.CONTENT_MATERIAL.equals(type)
                || ContentShare.CONTENT_ASSIGNMENT.equals(type);
    }

    private static boolean invalidScopeTarget(
            ContentShare value
    ) {
        if (ContentShare.SCOPE_MADRASSA.equals(value.scope)) {
            return !blank(value.targetId);
        }

        return blank(value.targetId);
    }

    private static boolean blank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}
