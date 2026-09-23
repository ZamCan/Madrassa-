package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Assessment;
import java.util.List;

public interface AssessmentStore {
    Assessment findById(String id);
    List<Assessment> findByStudent(String studentId);
    List<Assessment> findByAssignment(String assignmentId);
    List<Assessment> findByMadrassa(String madrassaId);
    boolean save(Assessment assessment);
    boolean update(Assessment assessment);
}
