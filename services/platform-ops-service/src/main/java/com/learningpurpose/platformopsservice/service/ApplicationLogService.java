package com.learningpurpose.platformopsservice.service;

import com.learningpurpose.platformopsservice.dto.ApplicationLogDto;
import com.learningpurpose.platformopsservice.entity.ApplicationLog;
import com.learningpurpose.platformopsservice.repository.ApplicationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationLogService {

    private final ApplicationLogRepository repository;

    public List<ApplicationLogDto> getLogs(LocalDate date, String serviceName, String logLevel){
        return repository.findLogsByDate(date)
                .stream()
                .filter(log -> serviceName == null || serviceName.isBlank() || serviceName.equalsIgnoreCase(log.getServiceName()))
                .filter(log -> logLevel == null || logLevel.isBlank() || logLevel.equalsIgnoreCase(log.getLogLevel()))
                .map(this::toDto).toList();
    }

    private ApplicationLogDto toDto(ApplicationLog log) {
        return new ApplicationLogDto(
                log.getKey().getLogDate(),
                log.getKey().getLogTime(),
                log.getServiceName(),
                log.getLogLevel(),
                log.getMessage(),
                log.getContainerId()
        );
    }

}
