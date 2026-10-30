package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "milestones")
public class Milestone {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Organization company;

    @Column(nullable = false)
    private String title;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 50)
    private String status = "PENDING"; // PENDING, REACHED, DELAYED

    @Column(name = "target_completion_pct")
    private Double targetCompletionPct = 0.0;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public Milestone() {
        this.id = "mls_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public Milestone(Project project, Organization company, String title, LocalDate dueDate, Double targetCompletionPct) {
        this();
        this.project = project;
        this.company = company;
        this.title = title;
        this.dueDate = dueDate;
        if (targetCompletionPct != null) {
            this.targetCompletionPct = targetCompletionPct;
        }
        this.status = "PENDING";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Organization getCompany() { return company; }
    public void setCompany(Organization company) { this.company = company; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTargetCompletionPct() { return targetCompletionPct; }
    public void setTargetCompletionPct(Double targetCompletionPct) { this.targetCompletionPct = targetCompletionPct; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
