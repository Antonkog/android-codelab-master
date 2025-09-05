package com.sap.codelab.core.data

import com.sap.codelab.core.data.db.AppDatabase
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.core.domain.toMemo
import com.sap.codelab.core.domain.toMemoEntity

/**
 * The repository is used to retrieve data from a data source.
 */
internal class Repository(private val database: AppDatabase) : IMemoRepository {

    override suspend fun saveMemo(memo: Memo) = database.getMemoDao().insert(memo.toMemoEntity())

    override suspend fun getOpen(): List<Memo> = database.getMemoDao().getOpen().map { it.toMemo() }

    override suspend fun getAll(): List<Memo> = database.getMemoDao().getAll().map { it.toMemo() }

    override suspend fun getMemoById(id: Long): Memo = database.getMemoDao().getMemoById(id).toMemo()
}