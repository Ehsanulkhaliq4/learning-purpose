package com.learningpurpose.platformopsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@PrimaryKeyClass
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApplicationLogKey implements Serializable {

    @PrimaryKeyColumn(
            name = "log_date",
            type = PrimaryKeyType.PARTITIONED,
            ordinal = 0
    )
    private LocalDate logDate;

    @PrimaryKeyColumn(
            name = "log_time",
            type = PrimaryKeyType.CLUSTERED,
            ordinal = 1
    )
    private Instant logTime;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ApplicationLogKey)) return false;

        ApplicationLogKey other = (ApplicationLogKey) obj;

        return Objects.equals(logDate, other.logDate)
                && Objects.equals(logTime, other.logTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logDate, logTime);
    }

}
