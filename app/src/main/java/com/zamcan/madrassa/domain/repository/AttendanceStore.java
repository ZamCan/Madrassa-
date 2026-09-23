package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Attendance;

import java.util.List;

public interface AttendanceStore {

    Attendance findById(String attendanceId);

    List<Attendance> findByStudent(
            String studentId
    );

    List<Attendance> findByClassAndDate(
            String classId,
            String date
    );

    boolean save(Attendance attendance);

    boolean update(Attendance attendance);
}
