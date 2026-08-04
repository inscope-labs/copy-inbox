package com.inscopelabs.abx.clipinbox.export.saf

import android.net.Uri
import com.inscopelabs.abx.clipinbox.data.local.NamingMacro
import com.inscopelabs.abx.clipinbox.data.local.NamingMacroDao
import com.inscopelabs.abx.clipinbox.data.local.SafPath
import com.inscopelabs.abx.clipinbox.data.local.SafPathDao
import kotlinx.coroutines.flow.Flow

class SafPathRepository(
    private val safPathDao: SafPathDao,
    private val namingMacroDao: NamingMacroDao,
) {
    fun observePaths(): Flow<List<SafPath>> = safPathDao.observeAll()
    fun observeMacros(): Flow<List<NamingMacro>> = namingMacroDao.observeAll()

    suspend fun addPath(label: String, treeUri: Uri): Long =
        safPathDao.insert(SafPath(label = label, treeUri = treeUri.toString()))

    suspend fun deletePath(path: SafPath) = safPathDao.delete(path)

    suspend fun addMacro(label: String, template: String): Long =
        namingMacroDao.insert(NamingMacro(label = label, template = template))

    suspend fun updateMacro(macro: NamingMacro) = namingMacroDao.update(macro)

    suspend fun deleteMacro(macro: NamingMacro) = namingMacroDao.delete(macro)

    suspend fun lastUsedPath(): SafPath? = safPathDao.lastUsed()

    suspend fun recordUse(path: SafPath) =
        safPathDao.recordUse(path.id, System.currentTimeMillis())
}
