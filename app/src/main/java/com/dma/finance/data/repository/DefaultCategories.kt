package com.dma.finance.data.repository

import com.dma.finance.data.local.entity.CategoryEntity
import com.dma.finance.data.local.entity.CategoryType

/** Catégories proposées automatiquement à la création d'un nouveau projet. */
object DefaultCategories {
    fun forProject(projectId: Long): List<CategoryEntity> = listOf(
        CategoryEntity(projectId = projectId, name = "Salaire", type = CategoryType.INCOME, colorHex = "#2E7D32", icon = "payments"),
        CategoryEntity(projectId = projectId, name = "Autres revenus", type = CategoryType.INCOME, colorHex = "#558B2F", icon = "savings"),
        CategoryEntity(projectId = projectId, name = "Alimentation", type = CategoryType.EXPENSE, colorHex = "#EF6C00", icon = "restaurant"),
        CategoryEntity(projectId = projectId, name = "Transport", type = CategoryType.EXPENSE, colorHex = "#6D4C41", icon = "directions_bus"),
        CategoryEntity(projectId = projectId, name = "Logement", type = CategoryType.EXPENSE, colorHex = "#5E35B1", icon = "home"),
        CategoryEntity(projectId = projectId, name = "Santé", type = CategoryType.EXPENSE, colorHex = "#D81B60", icon = "medical_services"),
        CategoryEntity(projectId = projectId, name = "Éducation", type = CategoryType.EXPENSE, colorHex = "#1565C0", icon = "school"),
        CategoryEntity(projectId = projectId, name = "Loisirs", type = CategoryType.EXPENSE, colorHex = "#00838F", icon = "sports_esports"),
        CategoryEntity(projectId = projectId, name = "Autres dépenses", type = CategoryType.EXPENSE, colorHex = "#616161", icon = "category")
    )
}
