package com.dma.finance.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dma.finance.data.local.dao.AccountDao
import com.dma.finance.data.local.dao.BudgetDao
import com.dma.finance.data.local.dao.CategoryDao
import com.dma.finance.data.local.dao.ProjectDao
import com.dma.finance.data.local.dao.ProjectMemberDao
import com.dma.finance.data.local.dao.TransactionDao
import com.dma.finance.data.local.dao.UserDao
import com.dma.finance.data.local.entity.AccountEntity
import com.dma.finance.data.local.entity.BudgetEntity
import com.dma.finance.data.local.entity.CategoryEntity
import com.dma.finance.data.local.entity.ProjectEntity
import com.dma.finance.data.local.entity.ProjectMemberEntity
import com.dma.finance.data.local.entity.TransactionEntity
import com.dma.finance.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        ProjectMemberEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun projectMemberDao(): ProjectMemberDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "gestion_finances.db"
    }
}
