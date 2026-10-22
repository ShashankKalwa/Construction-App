package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "calculations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calculation {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "project_id", nullable = false, length = 50)
    private String projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculator_type", nullable = false, length = 32)
    private CalculatorType calculatorType;

    @Column(nullable = false)
    private String title;

    @Column(name = "element_type", length = 64)
    private String elementType;

    @Column(name = "input_parameters", nullable = false, columnDefinition = "TEXT")
    private String inputParameters;

    @Column(name = "output_results", nullable = false, columnDefinition = "TEXT")
    private String outputResults;

    @Column(name = "primary_quantity", nullable = false)
    private Double primaryQuantity;

    @Column(name = "primary_unit", nullable = false, length = 20)
    private String primaryUnit;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
