package com.dma.finance.data.repository

import com.dma.finance.data.local.dao.CategoryDao
import com.dma.finance.data.local.dao.ProjectDao
import com.dma.finance.data.local.dao.ProjectMemberDao
import com.dma.finance.data.local.dao.UserDao
import com.dma.finance.data.local.entity.ProjectEntity
import com.dma.finance.data.local.entity.ProjectMemberEntity
import com.dma.finance.data.local.entity.ProjectRole
import com.dma.finance.data.local.relation.ProjectMemberWithUser
import com.dma.finance.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class ProjectMemberResult {
    object Success : ProjectMemberResult()
    object UserNotFound : ProjectMemberResult()
    object AlreadyMember : ProjectMemberResult()
    object LastOwner : ProjectMemberResult()
}

/**
 * Gère les projets financiers et leurs membres. C'est le cœur de la fonctionnalité
 * multi-utilisateurs : un projet peut être partagé avec d'autres comptes déjà inscrits
 * dans l'application, chacun avec un rôle ([ProjectRole]) qui borne ses permissions.
 */
@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val projectMemberDao: ProjectMemberDao,
    private val categoryDao: CategoryDao,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    val currentProjectId: Flow<Long?> = sessionManager.currentProjectId

    fun observeProjectsForUser(userId: Long): Flow<List<ProjectEntity>> =
        projectDao.observeProjectsForUser(userId)

    fun observeProject(projectId: Long): Flow<ProjectEntity?> = projectDao.observeById(projectId)

    fun observeMembers(projectId: Long): Flow<List<ProjectMemberWithUser>> =
        projectMemberDao.observeMembersForProject(projectId)

    fun observeMyRole(projectId: Long, userId: Long): Flow<ProjectMemberEntity?> =
        projectMemberDao.observeMember(projectId, userId)

    suspend fun createProject(
        name: String,
        description: String,
        currencyCode: String,
        ownerUserId: Long
    ): Long {
        val project = ProjectEntity(
            name = name.trim(),
            description = description.trim(),
            currencyCode = currencyCode,
            ownerId = ownerUserId
        )
        val projectId = projectDao.insert(project)
        projectMemberDao.insert(
            ProjectMemberEntity(projectId = projectId, userId = ownerUserId, role = ProjectRole.OWNER)
        )
        DefaultCategories.forProject(projectId).forEach { categoryDao.insert(it) }
        sessionManager.setCurrentProject(projectId)
        return projectId
    }

    suspend fun selectProject(projectId: Long) {
        sessionManager.setCurrentProject(projectId)
    }

    suspend fun addMemberByEmail(projectId: Long, email: String, role: ProjectRole): ProjectMemberResult {
        val user = userDao.findByEmail(email.trim()) ?: return ProjectMemberResult.UserNotFound
        val existing = projectMemberDao.findMember(projectId, user.id)
        if (existing != null) return ProjectMemberResult.AlreadyMember
        projectMemberDao.insert(ProjectMemberEntity(projectId = projectId, userId = user.id, role = role))
        return ProjectMemberResult.Success
    }

    suspend fun updateMemberRole(member: ProjectMemberEntity, newRole: ProjectRole): ProjectMemberResult {
        if (member.role == ProjectRole.OWNER && newRole != ProjectRole.OWNER) {
            if (projectMemberDao.countOwners(member.projectId) <= 1) return ProjectMemberResult.LastOwner
        }
        projectMemberDao.update(member.copy(role = newRole))
        return ProjectMemberResult.Success
    }

    suspend fun removeMember(member: ProjectMemberEntity): ProjectMemberResult {
        if (member.role == ProjectRole.OWNER && projectMemberDao.countOwners(member.projectId) <= 1) {
            return ProjectMemberResult.LastOwner
        }
        projectMemberDao.delete(member)
        return ProjectMemberResult.Success
    }

    suspend fun deleteProject(project: ProjectEntity) {
        projectDao.delete(project)
    }

    suspend fun updateProject(project: ProjectEntity) {
        projectDao.update(project)
    }
}
