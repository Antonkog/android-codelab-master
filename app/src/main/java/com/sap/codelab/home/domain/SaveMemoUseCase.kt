package com.sap.codelab.home.domain

import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo

class SaveMemoUseCase(private val repository: IMemoRepository){
    suspend operator fun invoke(memo: Memo) {
        repository.saveMemo(memo)
    }
}