package com.example.contruction_management_app.core.navigation

object AppRoutes {
    const val LOGIN = "login"
    const val MAIN_SHELL = "main_shell"
    const val GALLERY = "gallery"
    const val PROJECT_DETAIL = "project_detail/{projectId}"

    fun projectDetail(projectId: String): String = "project_detail/$projectId"
}
