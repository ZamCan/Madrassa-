package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.LearningProgress;
import java.util.List;

public interface LearningProgressStore {
    LearningProgress findById(String progressId);
    List<LearningProgress> findByLearner(String learnerId);
    List<LearningProgress> findByLesson(String lessonId);
    LearningProgress findByLearnerAndLesson(String learnerId, String lessonId);
    boolean save(LearningProgress progress);
    boolean update(LearningProgress progress);
}
