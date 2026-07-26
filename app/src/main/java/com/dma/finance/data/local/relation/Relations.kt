package com.dma.finance.data.local.relation

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.dma.finance.data.local.entity.ProjectMemberEntity
import com.dma.finance.data.local.entity.UserEntity

/** Un membre de projet accompagné des informations de l'utilisateur associé. */
data class ProjectMemberWithUser(
    @Embedded val member: ProjectMemberEntity,
    @Relation(parentColumn = "userId", entityColumn = "id")
    val user: UserEntity
)

/**
 * Vue "riche" d'une transaction pour l'affichage dans les listes, obtenue par une
 * jointure SQL (voir [com.dma.finance.data.local.dao.TransactionDao]) plutôt que par
 * une [Relation] Room, car elle combine plusieurs tables différentes.
 */
data class TransactionWithDetails(
    val id: Long,
    val projectId: Long,
    val accountId: Long,
    @ColumnInfo(name = "accountName") val accountName: String,
    val categoryId: Long?,
    @ColumnInfo(name = "categoryName") val categoryName: String?,
    @ColumnInfo(name = "categoryIcon") val categoryIcon: String?,
    @ColumnInfo(name = "categoryColor") val categoryColor: String?,
    val recordedByUserId: Long,
    @ColumnInfo(name = "recordedByName") val recordedByName: String,
    val type: String,
    val amountMinor: Long,
    val date: Long,
    val note: String,
    val transferToAccountId: Long?,
    val receiptPhotoPath: String?
)

/** Total dépensé/reçu pour une catégorie sur une période, utilisé par l'écran Rapports. */
data class CategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: String,
    val totalMinor: Long
)

/** Total des revenus et dépenses pour un mois donné (format "yyyy-MM"). */
data class MonthlySummary(
    val yearMonth: String,
    val incomeMinor: Long,
    val expenseMinor: Long
)
