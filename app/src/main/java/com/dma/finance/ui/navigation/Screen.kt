package com.dma.finance.ui.navigation

/** Routes de navigation de l'application. Les identifiants optionnels valent 0 quand absents. */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROJECT_LIST = "projects"
    const val PROJECT_CREATE = "project_create"

    const val HOME = "home/{projectId}"
    fun home(projectId: Long) = "home/$projectId"

    const val PROJECT_MEMBERS = "project_members/{projectId}"
    fun projectMembers(projectId: Long) = "project_members/$projectId"

    const val ADD_MEMBER = "add_member/{projectId}"
    fun addMember(projectId: Long) = "add_member/$projectId"

    const val ACCOUNT_LIST = "accounts/{projectId}"
    fun accountList(projectId: Long) = "accounts/$projectId"

    const val ACCOUNT_EDIT = "account_edit/{projectId}?accountId={accountId}"
    fun accountEdit(projectId: Long, accountId: Long = 0L) = "account_edit/$projectId?accountId=$accountId"

    const val CATEGORY_LIST = "categories/{projectId}"
    fun categoryList(projectId: Long) = "categories/$projectId"

    const val CATEGORY_EDIT = "category_edit/{projectId}?categoryId={categoryId}"
    fun categoryEdit(projectId: Long, categoryId: Long = 0L) = "category_edit/$projectId?categoryId=$categoryId"

    const val TRANSACTION_EDIT = "transaction_edit/{projectId}?transactionId={transactionId}"
    fun transactionEdit(projectId: Long, transactionId: Long = 0L) = "transaction_edit/$projectId?transactionId=$transactionId"

    const val BUDGET_EDIT = "budget_edit/{projectId}?budgetId={budgetId}"
    fun budgetEdit(projectId: Long, budgetId: Long = 0L) = "budget_edit/$projectId?budgetId=$budgetId"
}
