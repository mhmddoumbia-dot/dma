package com.dma.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dma.finance.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Insert
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    fun observeById(projectId: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    suspend fun findById(projectId: Long): ProjectEntity?

    @Query("SELECT * FROM projects WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun findByFirebaseId(firebaseId: String): ProjectEntity?

    /** Tous les projets auxquels l'utilisateur appartient, propriétaire ou simple membre. */
    @Query(
        """
        SELECT p.* FROM projects p
        INNER JOIN project_members pm ON pm.projectId = p.id
        WHERE pm.userId = :userId
        ORDER BY p.createdAt DESC
        """
    )
    fun observeProjectsForUser(userId: Long): Flow<List<ProjectEntity>>
}
