package com.zamcan.madrassa.domain.session;

import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.domain.authorization.parent.ParentAccessPolicy;
import com.zamcan.madrassa.domain.common.IdGenerator;

public final class ParentSessionFactory {


    public ParentSessionFactory() {
    }

    public ParentSession create(Parent parent) {
        if (parent == null || !ParentAccessPolicy.canLogin(parent)) {
            throw new SecurityException(
                    "Cannot create a session for an unauthorized parent."
            );
        }

        return new ParentSession(
                IdGenerator.newId(),
                parent.id,
                parent.madrassaId,
                parent.linkedStudentIds
        );
    }
}
