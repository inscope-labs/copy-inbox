package com.inscopelabs.abx.clipinbox.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NamingMacroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(macro: NamingMacro): Long

    @Update
    suspend fun update(macro: NamingMacro)

    @Delete
    suspend fun delete(macro: NamingMacro)

    @Query("SELECT * FROM naming_macros ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<NamingMacro>>
}
