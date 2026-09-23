package com.zamcan.madrassa.domain.authorization.parent;

import com.zamcan.madrassa.data.model.Parent;

public interface ParentIdentityResolver {

    Parent findByPhone(String phone);

    Parent resolve(String identifier);
}
