package com.dma.finance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un compte financier au sein d'un projet (espèces, banque, mobile money, carte...).
 * Le solde courant est recalculé à partir du solde initial et des transactions liées,
 * [initialBalanceMinor] est exprimé dans la plus petite unité de la devise (ex: centimes).
 */
@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val projectId: Long,
    val name: String,
    val type: AccountType,
    val initialBalanceMinor: Long = 0L,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
