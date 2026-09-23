package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Lesson;
import java.util.List;

public interface LessonStore {
    Lesson findById(String lessonId);
    List<Lesson> findByUnit(String unitId);
    boolean save(Lesson lesson);
    boolean update(Lesson lesson);
}
