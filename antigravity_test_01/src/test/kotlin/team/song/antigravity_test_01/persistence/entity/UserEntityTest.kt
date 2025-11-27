package team.song.antigravity_test_01.persistence.entity

import org.junit.jupiter.api.Test
import team.song.antigravity_test_01.domain.model.User
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserEntityTest {

    @Test
    fun shouldConvertUserEntityToDomainUser() {
        // Given
        val entity = UserEntity(
            id = 1L,
            firebaseUid = "firebase-uid-123",
            email = "test@example.com",
            displayName = "Test User",
            fcmToken = "fcm-token-123",
            createdAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            lastLoginAt = LocalDateTime.of(2024, 1, 2, 12, 0)
        )

        // When
        val domainUser = entity.toDomain()

        // Then
        assertNotNull(domainUser)
        assertEquals(entity.id, domainUser.id)
        assertEquals(entity.firebaseUid, domainUser.firebaseUid)
        assertEquals(entity.email, domainUser.email)
        assertEquals(entity.displayName, domainUser.displayName)
        assertEquals(entity.fcmToken, domainUser.fcmToken)
        assertEquals(entity.createdAt, domainUser.createdAt)
        assertEquals(entity.lastLoginAt, domainUser.lastLoginAt)
    }

    @Test
    fun shouldConvertDomainUserToUserEntity() {
        // Given
        val domainUser = User(
            id = 1L,
            firebaseUid = "firebase-uid-123",
            email = "test@example.com",
            displayName = "Test User",
            fcmToken = "fcm-token-123",
            createdAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            lastLoginAt = LocalDateTime.of(2024, 1, 2, 12, 0)
        )

        // When
        val entity = UserEntity.fromDomain(domainUser)

        // Then
        assertNotNull(entity)
        assertEquals(domainUser.id, entity.id)
        assertEquals(domainUser.firebaseUid, entity.firebaseUid)
        assertEquals(domainUser.email, entity.email)
        assertEquals(domainUser.displayName, entity.displayName)
        assertEquals(domainUser.fcmToken, entity.fcmToken)
        assertEquals(domainUser.createdAt, entity.createdAt)
        assertEquals(domainUser.lastLoginAt, entity.lastLoginAt)
    }
}
