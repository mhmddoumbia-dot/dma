package com.dma.finance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un projet financier (ex: "Famille", "Association", "Petit commerce").
 * Chaque projet possède ses propres comptes, catégories, transactions et budgets,
 * et peut être partagé entre plusieurs utilisateurs via [ProjectMemberEntity].
 */
@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId")]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val currencyCode: String = "XOF",
    val ownerId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
