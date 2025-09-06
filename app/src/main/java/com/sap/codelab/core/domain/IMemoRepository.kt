package com.sap.codelab.core.domain

import kotlinx.coroutines.flow.Flow


/**
 * Interface for a repository offering memo related CRUD operations.
 */
interface IMemoRepository {

    /**
     * Saves the given memo to the database.
     */
    suspend fun saveMemo(memo: Memo): Long

    /**
     * @return the memo whose id matches the given id.
     */
    suspend fun getMemoById(id: Long): Memo?

    /**
     * @return all memos currently in the database.
     */
    fun getAllMemoAsFlow(): Flow<List<Memo>>

    /**
     * @return all memos currently in the database, except those that have been marked as "done".
     */
    fun getOpenMemoAsFlow(): Flow<List<Memo>>
}