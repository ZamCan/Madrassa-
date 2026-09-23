package com.zamcan.madrassa.domain.academic;

import com.zamcan.madrassa.data.model.ClassGroup;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.repository.ClassStore;

import java.util.List;

public final class ClassManagementService {

    private final ClassStore classStore;

    public ClassManagementService(
            ClassStore classStore
    ) {
        if (classStore == null) {
            throw new IllegalArgumentException(
                    "classStore is required"
            );
        }

        this.classStore = classStore;
    }

    public boolean create(
            ClassGroup classGroup
    ) {
        validateForCreate(classGroup);

        if (!uniqueCode(
                classGroup.madrassaId,
                classGroup.code,
                null
        )) {
            throw new IllegalArgumentException(
                    "class code already exists in this madrassa"
            );
        }

        return classStore.save(classGroup);
    }

    public boolean update(
            ClassGroup classGroup
    ) {
        validateForUpdate(classGroup);

        ClassGroup existing =
                classStore.findById(classGroup.id);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "class does not exist"
            );
        }

        TenantPolicy.requireSameMadrassa(
                classGroup.madrassaId,
                existing.madrassaId
        );

        if (!uniqueCode(
                classGroup.madrassaId,
                classGroup.code,
                classGroup.id
        )) {
            throw new IllegalArgumentException(
                    "class code already exists in this madrassa"
            );
        }

        return classStore.update(classGroup);
    }

    public ClassGroup findById(
            String madrassaId,
            String classId
    ) {
        if (blank(madrassaId) || blank(classId)) {
            return null;
        }

        ClassGroup value =
                classStore.findById(classId.trim());

        if (value == null) {
            return null;
        }

        TenantPolicy.requireSameMadrassa(
                madrassaId.trim(),
                value.madrassaId
        );

        return value;
    }

    public List<ClassGroup> findActive(
            String madrassaId
    ) {
        if (blank(madrassaId)) {
            return java.util.Collections.emptyList();
        }

        return classStore.findActiveByMadrassa(
                madrassaId.trim()
        );
    }

    public boolean setActive(
            String madrassaId,
            String classId,
            boolean active
    ) {
        ClassGroup value =
                findById(madrassaId, classId);

        if (value == null) {
            throw new IllegalArgumentException(
                    "class does not exist"
            );
        }

        value.active = active;

        return classStore.update(value);
    }

    private boolean uniqueCode(
            String madrassaId,
            String code,
            String excludedId
    ) {
        if (blank(code)) {
            return true;
        }

        String normalized = code.trim();

        for (ClassGroup value :
                classStore.findByMadrassa(madrassaId.trim())) {

            if (value == null || blank(value.code)) {
                continue;
            }

            if (excludedId != null
                    && excludedId.equals(value.id)) {
                continue;
            }

            if (normalized.equalsIgnoreCase(
                    value.code.trim()
            )) {
                return false;
            }
        }

        return true;
    }

    private static void validateForCreate(
            ClassGroup value
    ) {
        validate(value);

        if (blank(value.id)) {
            throw new IllegalArgumentException(
                    "class id is required"
            );
        }
    }

    private static void validateForUpdate(
            ClassGroup value
    ) {
        validate(value);

        if (blank(value.id)) {
            throw new IllegalArgumentException(
                    "class id is required"
            );
        }
    }

    private static void validate(
            ClassGroup value
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "class is required"
            );
        }

        if (blank(value.madrassaId)) {
            throw new IllegalArgumentException(
                    "madrassa id is required"
            );
        }

        if (blank(value.name)) {
            throw new IllegalArgumentException(
                    "class name is required"
            );
        }

        if (value.code != null
                && value.code.trim().isEmpty()) {
            value.code = null;
        }

        if (value.code != null
                && value.code.trim().length() > 32) {
            throw new IllegalArgumentException(
                    "class code is too long"
            );
        }

        value.madrassaId =
                value.madrassaId.trim();

        value.name =
                value.name.trim();

        if (value.code != null) {
            value.code =
                    value.code.trim();
        }
    }

    private static boolean blank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }
}
