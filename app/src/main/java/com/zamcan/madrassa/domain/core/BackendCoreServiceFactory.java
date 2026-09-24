package com.zamcan.madrassa.domain.core;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.repository.AttendanceRepository;
import com.zamcan.madrassa.data.repository.ClassRepository;
import com.zamcan.madrassa.data.repository.FeeRepository;
import com.zamcan.madrassa.data.repository.SmsRepository;
import com.zamcan.madrassa.data.repository.StudentRepository;
import com.zamcan.madrassa.data.repository.ParentRepository;
import com.zamcan.madrassa.domain.authorization.MadrassaAccessContext;
import com.zamcan.madrassa.domain.parent.ParentStudentAccessService;
import com.zamcan.madrassa.domain.students.StudentClassAssignmentService;
import com.zamcan.madrassa.domain.attendance.AttendanceService;
import com.zamcan.madrassa.domain.communication.SmsQueueService;
import com.zamcan.madrassa.domain.communication.SmsDispatchService;
import com.zamcan.madrassa.domain.integration.SmsGateway;
import com.zamcan.madrassa.domain.finance.FeeService;

public final class BackendCoreServiceFactory {
    private BackendCoreServiceFactory() {}

    public static FeeService fees(EduNoorDatabase database, String madrassaId) {
        require(database, madrassaId);
        return new FeeService(
                new FeeRepository(database),
                new StudentRepository(database),
                madrassaId
        );
    }

    public static AttendanceService attendance(
            EduNoorDatabase database,
            String madrassaId
    ) {
        require(database, madrassaId);
        return new AttendanceService(
                new AttendanceRepository(database),
                new StudentRepository(database),
                new ClassRepository(database),
                madrassaId
        );
    }

    public static SmsQueueService sms(
            EduNoorDatabase database,
            String madrassaId
    ) {
        require(database, madrassaId);
        return new SmsQueueService(
                new SmsRepository(database),
                madrassaId
        );
    }

    public static SmsDispatchService smsDispatch(
            EduNoorDatabase database,
            String madrassaId,
            SmsGateway gateway
    ) {
        require(database, madrassaId);
        if (gateway == null) {
            throw new IllegalArgumentException("sms gateway is required");
        }
        SmsRepository repository = new SmsRepository(database);
        SmsQueueService queue = new SmsQueueService(repository, madrassaId);
        return new SmsDispatchService(
                repository,
                queue,
                gateway,
                madrassaId
        );
    }

    public static ParentStudentAccessService parentAccess(
            EduNoorDatabase database,
            String madrassaId
    ) {
        require(database, madrassaId);
        return new ParentStudentAccessService(
                new ParentRepository(database),
                new StudentRepository(database),
                MadrassaAccessContext.forMadrassa(madrassaId)
        );
    }

    public static StudentClassAssignmentService studentClassAssignment(
            EduNoorDatabase database,
            String madrassaId
    ) {
        require(database, madrassaId);
        return new StudentClassAssignmentService(
                new StudentRepository(database),
                new ClassRepository(database),
                MadrassaAccessContext.forMadrassa(madrassaId)
        );
    }

    private static void require(
            EduNoorDatabase database,
            String madrassaId
    ) {
        if (database == null) {
            throw new IllegalArgumentException("database is required");
        }
        if (madrassaId == null || madrassaId.trim().isEmpty()) {
            throw new IllegalArgumentException("madrassaId is required");
        }
    }
}
