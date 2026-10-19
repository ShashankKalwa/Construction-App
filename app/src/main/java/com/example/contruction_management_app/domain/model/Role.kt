package com.example.contruction_management_app.domain.model

enum class Role(
    val id: String,
    val displayName: String,
    val permissions: Set<Permission>
) {
    SUPER_ADMIN(
        id = "super_admin",
        displayName = "Super Admin",
        permissions = Permission.entries.toSet()
    ),
    COMPANY_ADMIN(
        id = "company_admin",
        displayName = "Company Admin",
        permissions = setOf(
            Permission.VIEW_PROJECTS,
            Permission.CREATE_PROJECTS,
            Permission.EDIT_PROJECTS,
            Permission.VIEW_FINANCE,
            Permission.MANAGE_TEAM,
            Permission.APPROVE_EXPENSES,
            Permission.APPROVE_BOQ,
            Permission.VIEW_REPORTS,
            Permission.VIEW_DOCUMENTS,
            Permission.AUDIT_LOG_VIEW
        )
    ),
    PROJECT_MANAGER(
        id = "project_manager",
        displayName = "Project Manager",
        permissions = setOf(
            Permission.VIEW_PROJECTS,
            Permission.EDIT_PROJECTS,
            Permission.VIEW_TASKS,
            Permission.MANAGE_TASKS,
            Permission.VIEW_FINANCE,
            Permission.APPROVE_EXPENSES,
            Permission.APPROVE_BOQ,
            Permission.VIEW_REPORTS,
            Permission.VIEW_DOCUMENTS,
            Permission.VIEW_INVENTORY
        )
    ),
    CIVIL_ENGINEER(
        id = "civil_engineer",
        displayName = "Civil Engineer",
        permissions = setOf(
            Permission.VIEW_PROJECTS,
            Permission.USE_CALCULATORS,
            Permission.TAKE_MEASUREMENTS,
            Permission.VIEW_BOQ,
            Permission.EDIT_BOQ,
            Permission.VIEW_DOCUMENTS,
            Permission.VIEW_TASKS
        )
    ),
    SITE_SUPERVISOR(
        id = "site_supervisor",
        displayName = "Site Supervisor",
        permissions = setOf(
            Permission.VIEW_PROJECTS,
            Permission.VIEW_TASKS,
            Permission.UPDATE_TASKS,
            Permission.DAILY_REPORT_WRITE,
            Permission.LABOR_ATTENDANCE_WRITE,
            Permission.REQUEST_MATERIAL,
            Permission.REPORT_ISSUES
        )
    ),
    STOREKEEPER(
        id = "storekeeper",
        displayName = "Storekeeper",
        permissions = setOf(
            Permission.VIEW_PROJECTS,
            Permission.VIEW_INVENTORY,
            Permission.ISSUE_MATERIAL,
            Permission.RECEIVE_GOODS,
            Permission.REQUEST_MATERIAL
        )
    ),
    ACCOUNTANT(
        id = "accountant",
        displayName = "Accountant",
        permissions = setOf(
            Permission.VIEW_PROJECTS,
            Permission.VIEW_FINANCE,
            Permission.SUBMIT_EXPENSE,
            Permission.VERIFY_EXPENSE,
            Permission.PROCESS_PAYMENTS,
            Permission.VIEW_REPORTS
        )
    ),
    CONTRACTOR(
        id = "contractor",
        displayName = "Contractor",
        permissions = setOf(
            Permission.VIEW_PROJECTS,
            Permission.VIEW_TASKS,
            Permission.UPDATE_TASKS,
            Permission.SUBMIT_BILLS
        )
    ),
    CLIENT_OWNER(
        id = "client_owner",
        displayName = "Client/Owner",
        permissions = setOf(
            Permission.VIEW_PROJECTS,
            Permission.VIEW_REPORTS,
            Permission.VIEW_FINANCE_SUMMARY,
            Permission.VIEW_DOCUMENTS
        )
    );

    fun hasPermission(permission: Permission): Boolean {
        return permissions.contains(permission)
    }

    companion object {
        fun fromId(id: String): Role {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SITE_SUPERVISOR
        }
    }
}

enum class Permission {
    VIEW_PROJECTS,
    CREATE_PROJECTS,
    EDIT_PROJECTS,
    VIEW_TASKS,
    MANAGE_TASKS,
    UPDATE_TASKS,
    USE_CALCULATORS,
    TAKE_MEASUREMENTS,
    VIEW_BOQ,
    EDIT_BOQ,
    APPROVE_BOQ,
    VIEW_INVENTORY,
    REQUEST_MATERIAL,
    ISSUE_MATERIAL,
    RECEIVE_GOODS,
    VIEW_FINANCE,
    VIEW_FINANCE_SUMMARY,
    SUBMIT_EXPENSE,
    VERIFY_EXPENSE,
    APPROVE_EXPENSES,
    PROCESS_PAYMENTS,
    SUBMIT_BILLS,
    DAILY_REPORT_WRITE,
    LABOR_ATTENDANCE_WRITE,
    REPORT_ISSUES,
    VIEW_DOCUMENTS,
    VIEW_REPORTS,
    MANAGE_TEAM,
    AUDIT_LOG_VIEW
}
