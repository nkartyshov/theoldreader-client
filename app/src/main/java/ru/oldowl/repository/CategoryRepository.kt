package ru.oldowl.repository

import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.db.dao.CategoryDao
import ru.oldowl.db.model.Category

interface CategoryRepository {

    suspend fun findById(id: String): Category

    suspend fun saveOrUpdate(category: Category)

    suspend fun downloadCategory(): List<Category>

    class CategoryRepositoryImpl(
            private val categoryDao: CategoryDao,
            private val theOldReaderApi: TheOldReaderApi
    ) : CategoryRepository {

        override suspend fun findById(id: String): Category = categoryDao.findById(id)

        override suspend fun saveOrUpdate(category: Category) {
            if (categoryDao.exists(category.id))
                categoryDao.update(category)
            else categoryDao.save(category)
        }

        override suspend fun downloadCategory(): List<Category> =
                theOldReaderApi.getCategories()

    }
}