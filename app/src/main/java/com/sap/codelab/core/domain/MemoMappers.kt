package com.sap.codelab.core.domain

import com.sap.codelab.core.data.db.MemoEntity

/**
 * Mapping functions between data layer MemoEntity and domain layer Memo.
 */
fun MemoEntity.toMemo(): Memo = Memo(
    id = id,
    title = title,
    description = description,
    reminderDate = reminderDate,
    reminderLatitude = reminderLatitude,
    reminderLongitude = reminderLongitude,
    isDone = isDone,
    isNotificationShown = isNotificationShown
)

fun Memo.toMemoEntity(): MemoEntity = MemoEntity(
    id = id,
    title = title,
    description = description,
    reminderDate = reminderDate,
    reminderLatitude = reminderLatitude,
    reminderLongitude = reminderLongitude,
    isDone = isDone,
    isNotificationShown = isNotificationShown
)
