package com.sap.codelab.home.domain

import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import kotlinx.coroutines.flow.Flow

class LoadMemosUseCase(private val repository: IMemoRepository) {
    operator fun invoke(showAll: Boolean): Flow<List<Memo>> {
        return if(showAll){
            repository.getAllMemoAsFlow()
        }
        else{
            repository.getOpenMemoAsFlow()
        }
    }
}