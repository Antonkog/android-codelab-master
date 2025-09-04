package com.sap.codelab.core.data

import androidx.annotation.WorkerThread
import com.sap.codelab.core.data.db.Database
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo

/**
 * The repository is used to retrieve data from a data source.
 */
internal class Repository(private val database: Database) : IMemoRepository {

    @WorkerThread
    override fun saveMemo(memo: Memo) {
        database.getMemoDao().insert(memo)
    }

    @WorkerThread
    override fun getOpen(): List<Memo> = database.getMemoDao().getOpen()

    @WorkerThread
    override fun getAll(): List<Memo> = database.getMemoDao().getAll()

    @WorkerThread
    override fun getMemoById(id: Long): Memo = database.getMemoDao().getMemoById(id)
}