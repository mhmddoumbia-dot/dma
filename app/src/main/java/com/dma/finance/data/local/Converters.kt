package com.dma.finance.data.local

import androidx.room.TypeConverter
import com.dma.finance.data.local.entity.AccountType
import com.dma.finance.data.local.entity.BudgetPeriod
import com.dma.finance.data.local.entity.CategoryType
import com.dma.finance.data.local.entity.ProjectRole
import com.dma.finance.data.local.entity.TransactionType

/** Convertit les enums du modèle en `String` pour le stockage Room, et inversement. */
class Converters {

    @TypeConverter
    fun fromProjectRole(role: ProjectRole): String = role.name

    @TypeConverter
    fun toProjectRole(value: String): ProjectRole = ProjectRole.valueOf(value)

    @TypeConverter
    fun fromAccountType(type: AccountType): String = type.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun fromCategoryType(type: CategoryType): String = type.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType = CategoryType.valueOf(value)

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromBudgetPeriod(period: BudgetPeriod): String = period.name

    @TypeConverter
    fun toBudgetPeriod(value: String): BudgetPeriod = BudgetPeriod.valueOf(value)
}
