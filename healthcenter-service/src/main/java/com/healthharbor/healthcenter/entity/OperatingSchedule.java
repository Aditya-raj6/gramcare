package com.healthharbor.healthcenter.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Opening hours of a centre, or a duty roster entry when a worker is set. */
@Getter
@Setter
@Entity
@Table(name = "operating_schedules", indexes = @Index(columnList = "centreId"))
public class OperatingSchedule extends BaseEntity {

    @NotNull
    private Long centreId;
    private String centreName;

    private Long workerId;
    private String workerName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private DayOfWeek dayOfWeek;

    private LocalTime openTime;
    private LocalTime closeTime;

    private Boolean closed = false;

    private String notes;
}
