package com.inscopelabs.abx.clipinbox.category

import com.inscopelabs.abx.clipinbox.data.local.CategoryDao
import com.inscopelabs.abx.clipinbox.data.local.CategoryEntity
import com.inscopelabs.abx.clipinbox.data.local.ClipDao
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val clipDao: ClipDao
) : CategoryRepository {

    companion object {
        private const val TAG = "CategoryRepositoryImpl"
    }

    override fun observeCategories(): Flow<List<CategoryEntity>> {
        Logger.d(TAG, "observeCategories called")
        return categoryDao.observeAll()
    }

    override suspend fun getDefaultCategory(): CategoryEntity {
        Logger.d(TAG, "getDefaultCategory called")
        var defaultCat = categoryDao.getDefault()
        if (defaultCat == null) {
            Logger.w(TAG, "getDefaultCategory: no default category found, ensuring seed exists")
            ensureSeedCategoryExists()
            defaultCat = categoryDao.getDefault()
                ?: throw IllegalStateException("Default category missing after seed insertion")
        }
        Logger.i(TAG, "getDefaultCategory returned id: ${defaultCat.id}, name: ${defaultCat.name}")
        return defaultCat
    }

    override suspend fun addCategory(name: String, colorHex: String): Long {
        Logger.i(TAG, "addCategory name: $name, colorHex: $colorHex")
        val entity = CategoryEntity(
            name = name,
            colorHex = colorHex,
            isDefault = false,
            sortOrder = 0,
            createdAt = System.currentTimeMillis()
        )
        val id = categoryDao.insert(entity)
        Logger.i(TAG, "addCategory inserted new category id: $id")
        return id
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        Logger.i(TAG, "updateCategory id: ${category.id}, name: ${category.name}")
        categoryDao.update(category)
    }

    override suspend fun deleteCategory(category: CategoryEntity): Boolean {
        Logger.i(TAG, "deleteCategory id: ${category.id}, name: ${category.name}, isDefault: ${category.isDefault}")
        if (category.isDefault) {
            Logger.w(TAG, "deleteCategory aborted: cannot delete default category id: ${category.id}")
            return false
        }
        val defaultCategory = getDefaultCategory()
        Logger.i(TAG, "deleteCategory reassigning clips from category ${category.id} to default category ${defaultCategory.id}")
        clipDao.reassignCategory(category.id, defaultCategory.id)
        categoryDao.delete(category)
        Logger.i(TAG, "deleteCategory successfully deleted category id: ${category.id}")
        return true
    }

    override suspend fun setDefaultCategory(category: CategoryEntity) {
        Logger.i(TAG, "setDefaultCategory id: ${category.id}, name: ${category.name}")
        categoryDao.clearDefaultFlag()
        categoryDao.update(category.copy(isDefault = true))
        Logger.i(TAG, "setDefaultCategory completed for id: ${category.id}")
    }

    override suspend fun ensureSeedCategoryExists() {
        val count = categoryDao.countAll()
        Logger.d(TAG, "ensureSeedCategoryExists count: $count")
        if (count == 0) {
            val seed = CategoryEntity(
                name = "Uncategorized",
                colorHex = "#9E9E9E",
                isDefault = true,
                sortOrder = 0,
                createdAt = System.currentTimeMillis()
            )
            val id = categoryDao.insert(seed)
            Logger.i(TAG, "ensureSeedCategoryExists inserted seed category id: $id")
        }
    }
}
