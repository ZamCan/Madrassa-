package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.ContentShare;

import java.util.List;

public interface ContentShareStore {

    ContentShare findById(String id);

    List<ContentShare> findByContent(
            String contentType,
            String contentId
    );

    List<ContentShare> findByMadrassa(
            String madrassaId
    );

    List<ContentShare> findActiveForStudent(
            String madrassaId,
            String studentId
    );

    boolean save(ContentShare share);

    boolean update(ContentShare share);
}
