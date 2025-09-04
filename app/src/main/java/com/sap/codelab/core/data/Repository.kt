package com.sap.codelab.core.data

import com.sap.codelab.core.data.db.Database
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo

/**
 * The repository is used to retrieve data from a data source.
 */
internal class Repository(private val database: Database) : IMemoRepository {

    override suspend fun saveMemo(memo: Memo) = database.getMemoDao().insert(memo)

    override suspend fun getOpen(): List<Memo> = database.getMemoDao().getOpen()

    override suspend fun getAll(): List<Memo> = database.getMemoDao().getAll()

    override suspend fun getMemoById(id: Long): Memo = database.getMemoDao().getMemoById(id)
}