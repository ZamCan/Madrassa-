package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.LearningMaterial;
import java.util.List;

public interface LearningMaterialStore {
    LearningMaterial findById(String id);
    List<LearningMaterial> findByProgramme(String programmeId);
    List<LearningMaterial> findByMadrassa(String madrassaId);
    boolean save(LearningMaterial material);
    boolean update(LearningMaterial material);
}
