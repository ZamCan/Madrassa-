package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.AcademicUnit;
import java.util.List;

public interface AcademicUnitStore {
    AcademicUnit findById(String unitId);
    List<AcademicUnit> findByCourse(String courseId);
    boolean save(AcademicUnit unit);
    boolean update(AcademicUnit unit);
}
