package com.dma.finance.data.local.entity

/** Rôle d'un utilisateur au sein d'un projet financier partagé. */
enum class ProjectRole {
    OWNER,
    ADMIN,
    MEMBER,
    VIEWER;

    /** Un rôle peut-il modifier les données (comptes, transactions, budgets) du projet ? */
    fun canEdit(): Boolean = this != VIEWER

    /** Un rôle peut-il gérer les membres et supprimer le projet ? */
    fun canManageMembers(): Boolean = this == OWNER || this == ADMIN
}

enum class AccountType {
    CASH,
    BANK,
    MOBILE_MONEY,
    CARD,
    SAVINGS,
    OTHER
}

enum class CategoryType {
    INCOME,
    EXPENSE
}

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

enum class BudgetPeriod {
    WEEKLY,
    MONTHLY,
    YEARLY
}
