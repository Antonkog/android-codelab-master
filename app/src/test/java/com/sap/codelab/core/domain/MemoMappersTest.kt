package com.sap.codelab.core.domain

import com.google.common.truth.Truth.assertThat
import com.sap.codelab.core.data.db.MemoEntity
import org.junit.Test

class MemoMappersTest {

    @Test
    fun `entity to domain and back preserves fields`() {
        val entity = MemoEntity(
            id = 42L,
            title = "Title",
            description = "Desc",
            reminderDate = 12345L,
            reminderLatitude = 1.23,
            reminderLongitude = 4.56,
            isDone = false,
            notificationShown = true
        )

        val domain = entity.toMemo()
        assertThat(domain.id).isEqualTo(42L)
        assertThat(domain.title).isEqualTo("Title")
        assertThat(domain.description).isEqualTo("Desc")
        assertThat(domain.reminderDate).isEqualTo(12345L)
        assertThat(domain.reminderLatitude).isWithin(1e-9).of(1.23)
        assertThat(domain.reminderLongitude).isWithin(1e-9).of(4.56)
        assertThat(domain.isDone).isFalse()
        assertThat(domain.notificationShown).isTrue()

        val roundTrip = domain.toMemoEntity()
        assertThat(roundTrip).isEqualTo(entity)
    }
}
