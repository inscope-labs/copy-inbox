package com.inscopelabs.abx.clipinbox.tag

import com.inscopelabs.abx.clipinbox.data.local.ClipDao
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.data.local.ClipTagCrossRef
import com.inscopelabs.abx.clipinbox.data.local.TagDao
import com.inscopelabs.abx.clipinbox.data.local.TagEntity
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TagRepositoryImpl(
    private val tagDao: TagDao,
    private val clipDao: ClipDao
) : TagRepository {

    companion object {
        private const val TAG = "TagRepositoryImpl"
    }

    override fun observeTags(): Flow<List<TagEntity>> {
        Logger.d(TAG, "observeTags called")
        return tagDao.observeAll()
    }

    override suspend fun createTag(label: String, colorHex: String): Long {
        Logger.i(TAG, "createTag label: $label, colorHex: $colorHex")
        val isSys = SystemTags.isSystemReservedLabel(label)
        val entity = TagEntity(
            label = label,
            colorHex = colorHex,
            isSystemReserved = isSys,
            isDeleted = false,
            createdAt = System.currentTimeMillis()
        )
        val id = tagDao.insert(entity)
        Logger.i(TAG, "createTag inserted new tag id: $id, isSystemReserved: $isSys")
        return id
    }

    override suspend fun deleteTag(tagId: Long) {
        Logger.i(TAG, "deleteTag requested for tagId: $tagId")
        val tag = tagDao.getById(tagId)
        if (tag == null) {
            Logger.w(TAG, "deleteTag tag not found for id: $tagId")
            return
        }
        if (tag.isSystemReserved || SystemTags.isSystemReservedId(tagId)) {
            Logger.w(TAG, "deleteTag refused: tag $tagId ('${tag.label}') is system-reserved")
            return
        }
        tagDao.softDelete(tagId)
        Logger.i(TAG, "deleteTag soft-deleted tag id: $tagId")
    }

    override suspend fun addTagToClip(clipId: Long, tagId: Long) {
        Logger.i(TAG, "addTagToClip clipId: $clipId, tagId: $tagId")
        val ref = ClipTagCrossRef(clipId = clipId, tagId = tagId)
        tagDao.insertCrossRef(ref)
        Logger.i(TAG, "addTagToClip inserted cross ref clipId: $clipId, tagId: $tagId")
    }

    override suspend fun removeTagFromClip(clipId: Long, tagId: Long) {
        Logger.i(TAG, "removeTagFromClip clipId: $clipId, tagId: $tagId")
        tagDao.deleteCrossRef(clipId, tagId)
        Logger.i(TAG, "removeTagFromClip deleted cross ref clipId: $clipId, tagId: $tagId")
    }

    override fun observeTagsForClip(clipId: Long): Flow<List<TagEntity>> {
        Logger.d(TAG, "observeTagsForClip clipId: $clipId")
        return tagDao.observeTagsForClip(clipId)
    }

    override fun observeClipsForTags(tagIds: Set<Long>, matchAll: Boolean): Flow<List<ClipEntity>> {
        Logger.d(TAG, "observeClipsForTags tagIds: $tagIds, matchAll: $matchAll")
        if (tagIds.isEmpty()) {
            return flowOf(emptyList())
        }
        val tagList = tagIds.toList()
        return if (matchAll) {
            tagDao.observeClipsForTagsAll(tagList, tagList.size)
        } else {
            tagDao.observeClipsForTagsAny(tagList)
        }
    }

    override suspend fun ensureSystemTagsSeeded() {
        val count = tagDao.countAll()
        Logger.d(TAG, "ensureSystemTagsSeeded count: $count")
        if (count == 0) {
            val now = System.currentTimeMillis()
            SystemTags.ALL_SYSTEM_TAGS.forEach { sysTag ->
                val entity = TagEntity(
                    id = sysTag.id,
                    label = sysTag.label,
                    colorHex = sysTag.colorHex,
                    isSystemReserved = true,
                    isDeleted = false,
                    createdAt = now
                )
                val id = tagDao.insert(entity)
                Logger.i(TAG, "ensureSystemTagsSeeded inserted sys tag id: $id, label: ${sysTag.label}")
            }
        }
    }
}
