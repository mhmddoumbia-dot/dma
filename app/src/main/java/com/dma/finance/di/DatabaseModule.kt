package com.dma.finance.di

import android.content.Context
import androidx.room.Room
import com.dma.finance.data.local.AppDatabase
import com.dma.finance.data.local.dao.AccountDao
import com.dma.finance.data.local.dao.BudgetDao
import com.dma.finance.data.local.dao.CategoryDao
import com.dma.finance.data.local.dao.ProjectDao
import com.dma.finance.data.local.dao.ProjectMemberDao
import com.dma.finance.data.local.dao.TransactionDao
import com.dma.finance.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()

    @Provides
    fun provideProjectMemberDao(db: AppDatabase): ProjectMemberDao = db.projectMemberDao()

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()
}
