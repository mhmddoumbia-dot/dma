package com.dma.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dma.finance.data.local.entity.ProjectMemberEntity
import com.dma.finance.data.local.relation.ProjectMemberWithUser
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectMemberDao {

    @Insert
    suspend fun insert(member: ProjectMemberEntity): Long

    @Update
    suspend fun update(member: ProjectMemberEntity)

    @Delete
    suspend fun delete(member: ProjectMemberEntity)

    @Transaction
    @Query("SELECT * FROM project_members WHERE projectId = :projectId ORDER BY joinedAt ASC")
    fun observeMembersForProject(projectId: Long): Flow<List<ProjectMemberWithUser>>

    @Query("SELECT * FROM project_members WHERE projectId = :projectId AND userId = :userId LIMIT 1")
    suspend fun findMember(projectId: Long, userId: Long): ProjectMemberEntity?

    @Query("SELECT * FROM project_members WHERE projectId = :projectId AND userId = :userId LIMIT 1")
    fun observeMember(projectId: Long, userId: Long): Flow<ProjectMemberEntity?>

    @Query("SELECT COUNT(*) FROM project_members WHERE projectId = :projectId AND role = 'OWNER'")
    suspend fun countOwners(projectId: Long): Int
}
