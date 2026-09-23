package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Assignment;
import java.util.List;

public interface AssignmentStore {
    Assignment findById(String id);
    List<Assignment> findByProgramme(String programmeId);
    List<Assignment> findByLesson(String lessonId);
    List<Assignment> findByMadrassa(String madrassaId);
    boolean save(Assignment assignment);
    boolean update(Assignment assignment);
}
