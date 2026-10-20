package com.example.contruction_management_app.backend.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "organizations")
class Organization(
    @Id
    @Column(length = 50)
    var id: String = "org_${UUID.randomUUID().toString().replace("-", "").take(12)}",

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true, length = 100)
    var slug: String,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "permissions")
class Permission(
    @Id
    @Column(length = 100)
    var id: String,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 50)
    var module: String
)

@Entity
@Table(name = "roles")
class Role(
    @Id
    @Column(length = 50)
    var id: String,

    @Column(nullable = false, length = 100)
    var name: String,

    var description: String? = null,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    var permissions: MutableSet<Permission> = mutableSetOf()
)

@Entity
@Table(name = "users")
class User(
    @Id
    @Column(length = 50)
    var id: String = "usr_${UUID.randomUUID().toString().replace("-", "").take(12)}",

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "full_name", nullable = false)
    var fullName: String,

    @Column(name = "hashed_password", nullable = false)
    var hashedPassword: String,

    @Column(name = "avatar_url", length = 500)
    var avatarUrl: String? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: Organization,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    var role: Role
)

@Entity
@Table(name = "projects")
class Project(
    @Id
    @Column(length = 50)
    var id: String = "prj_${UUID.randomUUID().toString().replace("-", "").take(12)}",

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, length = 50)
    var code: String,

    @Column(nullable = false)
    var location: String,

    @Column(name = "budget_inr", nullable = false, precision = 14, scale = 2)
    var budgetInr: BigDecimal,

    @Column(length = 50)
    var status: String = "Active",

    @Column(name = "progress_pct")
    var progressPct: Double = 0.0,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: Organization
)

@Entity
@Table(
    name = "project_members",
    uniqueConstraints = [UniqueConstraint(columnNames = ["project_id", "user_id"])]
)
class ProjectMember(
    @Id
    @Column(length = 50)
    var id: String = "pm_${UUID.randomUUID().toString().replace("-", "").take(12)}",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Project,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "assigned_role", length = 50)
    var assignedRole: String? = null,

    @Column(name = "joined_at")
    var joinedAt: Instant = Instant.now()
)

@Entity
@Table(name = "phases")
class Phase(
    @Id
    @Column(length = 50)
    var id: String = "phs_${UUID.randomUUID().toString().replace("-", "").take(12)}",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Project,

    @Column(nullable = false)
    var name: String,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,

    @Column(name = "start_date")
    var startDate: java.time.LocalDate? = null,

    @Column(name = "end_date")
    var endDate: java.time.LocalDate? = null,

    @Column(length = 50)
    var status: String = "Active",

    @Column(name = "progress_pct")
    var progressPct: Double = 0.0,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "phase", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var tasks: MutableList<Task> = mutableListOf()
)

@Entity
@Table(name = "tasks")
class Task(
    @Id
    @Column(length = 50)
    var id: String = "tsk_${UUID.randomUUID().toString().replace("-", "").take(12)}",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id", nullable = false)
    var phase: Phase,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Project,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    var assignee: User? = null,

    @Column(length = 20)
    var priority: String = "MEDIUM",

    @Column(length = 50)
    var status: String = "PENDING",

    @Column(name = "start_date")
    var startDate: java.time.LocalDate? = null,

    @Column(name = "due_date")
    var dueDate: java.time.LocalDate? = null,

    @Column(name = "progress_pct")
    var progressPct: Double = 0.0,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
)
