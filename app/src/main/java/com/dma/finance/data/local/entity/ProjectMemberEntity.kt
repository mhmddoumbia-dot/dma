package com.dma.finance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Association entre un utilisateur et un projet, avec un rôle. C'est cette table
 * qui rend l'application multi-utilisateurs : plusieurs personnes peuvent
 * collaborer sur un même projet financier avec des permissions différentes.
 */
@Entity(
    tableName = "project_members",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId", "userId"], unique = true), Index("userId")]
)
data class ProjectMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val projectId: Long,
    val userId: Long,
    val role: ProjectRole,
    val joinedAt: Long = System.currentTimeMillis()
)
