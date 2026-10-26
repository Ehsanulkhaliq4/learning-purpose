package com.learningpurpose.platformopsservice.repository;

import com.learningpurpose.platformopsservice.entity.ApplicationLog;
import com.learningpurpose.platformopsservice.entity.ApplicationLogKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ApplicationLogRepository extends CassandraRepository<ApplicationLog, ApplicationLogKey> {

    @Query("""
        SELECT * FROM logs.application_logs
        WHERE log_date = ?0
        LIMIT 500
        """)
    List<ApplicationLog> findLogsByDate(LocalDate logDate);

}
