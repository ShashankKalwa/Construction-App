package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String location;

    @Column(name = "budget_inr", nullable = false, precision = 14, scale = 2)
    private BigDecimal budgetInr;

    @Column(length = 50)
    private String status = "Active";

    @Column(name = "progress_pct")
    private Double progressPct = 0.0;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public Project() {
        this.id = "prj_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public Project(String name, String code, String location, BigDecimal budgetInr, Organization organization) {
        this();
        this.name = name;
        this.code = code;
        this.location = location;
        this.budgetInr = budgetInr;
        this.organization = organization;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getBudgetInr() { return budgetInr; }
    public void setBudgetInr(BigDecimal budgetInr) { this.budgetInr = budgetInr; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getProgressPct() { return progressPct; }
    public void setProgressPct(Double progressPct) { this.progressPct = progressPct; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }
}
