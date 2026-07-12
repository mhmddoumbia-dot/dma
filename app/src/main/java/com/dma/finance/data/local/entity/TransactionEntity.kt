package com.dma.finance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Une opération financière : revenu, dépense ou virement entre deux comptes
 * du même projet. [amountMinor] est toujours positif ; le sens est donné par [type].
 * [recordedByUserId] permet de savoir, dans un projet partagé, qui a saisi l'opération.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordedByUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("projectId"),
        Index("accountId"),
        Index("categoryId"),
        Index("recordedByUserId")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val projectId: Long,
    val accountId: Long,
    val categoryId: Long?,
    val recordedByUserId: Long,
    val type: TransactionType,
    val amountMinor: Long,
    val date: Long,
    val note: String = "",
    /** Renseigné uniquement pour un [TransactionType.TRANSFER] : compte de destination. */
    val transferToAccountId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
