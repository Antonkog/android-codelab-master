package com.sap.codelab.core.data

import com.sap.codelab.core.data.db.AppDatabase
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.core.domain.toMemo
import com.sap.codelab.core.domain.toMemoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The repository is used to retrieve data from a data source.
 */
internal class Repository(private val database: AppDatabase) : IMemoRepository {

    override suspend fun saveMemo(memo: Memo) = database.getMemoDao().insert(memo.toMemoEntity())

    override suspend fun getMemoById(id: Long): Memo = database.getMemoDao().getMemoById(id).toMemo()

    override fun getOpenMemoAsFlow(): Flow<List<Memo>> =
        database.getMemoDao()
            .getOpenAsFlow()
            .map { list -> list.map { it.toMemo() } }
            .distinctUntilChanged()


    override fun getAllMemoAsFlow(): Flow<List<Memo>> = database.getMemoDao()
        .getAllAsFlow()
        .map { list -> list.map { it.toMemo() } }
}