package com.zamcan.madrassa.domain.session;

import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.domain.common.IdGenerator;

public final class ParentSessionFactory {


    public ParentSessionFactory() {
    }

    public ParentSession create(Parent parent) {
        if (parent == null || !parent.active) {
            throw new SecurityException(
                    "Cannot create a session for inactive parent."
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
