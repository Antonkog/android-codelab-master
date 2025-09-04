package com.sap.codelab.core.domain


/**
 * Interface for a repository offering memo related CRUD operations.
 */
interface IMemoRepository {

    /**
     * Saves the given memo to the database.
     */
    suspend fun saveMemo(memo: Memo)

    /**
     * @return all memos currently in the database.
     */
    suspend fun getAll(): List<Memo>

    /**
     * @return all memos currently in the database, except those that have been marked as "done".
     */
    suspend fun getOpen(): List<Memo>

    /**
     * @return the memo whose id matches the given id.
     */
    suspend fun getMemoById(id: Long): Memo
}