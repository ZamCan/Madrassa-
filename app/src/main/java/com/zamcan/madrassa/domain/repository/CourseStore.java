package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Course;
import java.util.List;

public interface CourseStore {
    Course findById(String courseId);
    List<Course> findByMadrassa(String madrassaId);
    List<Course> findByProgramme(String programmeId);
    boolean save(Course course);
    boolean update(Course course);
}
