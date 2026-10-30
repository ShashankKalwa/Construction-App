package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "project_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"})
)
public class ProjectMember {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "assigned_role", length = 50)
    private String assignedRole;

    @Column(length = 30, nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, REMOVED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Organization company;

    @Column(name = "joined_at")
    private Instant joinedAt = Instant.now();

    public ProjectMember() {
        this.id = "pm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public ProjectMember(Project project, User user, String assignedRole) {
        this();
        this.project = project;
        this.user = user;
        this.assignedRole = assignedRole;
        if (project != null) {
            this.company = project.getOrganization();
        }
        this.status = "ACTIVE";
    }

    public ProjectMember(Project project, User user, String assignedRole, Organization company, String status) {
        this();
        this.project = project;
        this.user = user;
        this.assignedRole = assignedRole;
        this.company = company != null ? company : (project != null ? project.getOrganization() : null);
        this.status = status != null ? status : "ACTIVE";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAssignedRole() { return assignedRole; }
    public void setAssignedRole(String assignedRole) { this.assignedRole = assignedRole; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Organization getCompany() { return company; }
    public void setCompany(Organization company) { this.company = company; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}
