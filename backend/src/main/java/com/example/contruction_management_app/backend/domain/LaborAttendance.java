package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "labor_attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaborAttendance {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private DailySiteReport report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LaborTrade trade;

    @Column(nullable = false)
    private Integer headcount;

    @Column(name = "hours_worked", nullable = false, precision = 4, scale = 1)
    private BigDecimal hoursWorked;

    @Column(name = "contractor_name", length = 128)
    private String contractorName;
}
