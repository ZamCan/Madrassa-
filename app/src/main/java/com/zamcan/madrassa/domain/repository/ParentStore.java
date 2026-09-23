package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Parent;

public interface ParentStore {

    Parent findById(String parentId);

    Parent findByPhone(String phone);

    boolean existsByPhone(String phone);

    void save(Parent parent);

    void update(Parent parent);
}
