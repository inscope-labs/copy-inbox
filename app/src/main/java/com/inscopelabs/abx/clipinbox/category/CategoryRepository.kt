package com.inscopelabs.abx.clipinbox.category

import com.inscopelabs.abx.clipinbox.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<CategoryEntity>>
    suspend fun getDefaultCategory(): CategoryEntity
    suspend fun addCategory(name: String, colorHex: String): Long
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity): Boolean
    suspend fun setDefaultCategory(category: CategoryEntity)
    suspend fun ensureSeedCategoryExists()
}
