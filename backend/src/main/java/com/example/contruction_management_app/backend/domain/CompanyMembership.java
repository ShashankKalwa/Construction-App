package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "company_memberships",
    uniqueConstraints = @UniqueConstraint(name = "uk_org_user", columnNames = {"organization_id", "user_id"})
)
public class CompanyMembership {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(length = 30, nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INVITED, SUSPENDED

    @Column(name = "joined_at")
    private Instant joinedAt = Instant.now();

    public CompanyMembership() {
        this.id = "cm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public CompanyMembership(Organization organization, User user, Role role, String status) {
        this();
        this.organization = organization;
        this.user = user;
        this.role = role;
        this.status = status != null ? status : "ACTIVE";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}
