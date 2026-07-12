package com.dma.finance.ui.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.local.entity.CategoryEntity
import com.dma.finance.data.local.entity.CategoryType
import com.dma.finance.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])
    private val categoryId: Long? = (savedStateHandle.get<Long>("categoryId"))?.takeIf { it > 0 }
    val isEditing: Boolean get() = categoryId != null

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun loadExisting(onLoaded: (CategoryEntity) -> Unit) {
        val id = categoryId ?: return
        viewModelScope.launch {
            val category = categoryRepository.observeCategoriesForProject(projectId).first()
                .firstOrNull { it.id == id }
            category?.let(onLoaded)
        }
    }

    fun save(name: String, type: CategoryType, colorHex: String, icon: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            if (isEditing && categoryId != null) {
                categoryRepository.updateCategory(
                    CategoryEntity(id = categoryId, projectId = projectId, name = name, type = type, colorHex = colorHex, icon = icon)
                )
            } else {
                categoryRepository.createCategory(
                    CategoryEntity(projectId = projectId, name = name, type = type, colorHex = colorHex, icon = icon)
                )
            }
            _saved.value = true
        }
    }
}
