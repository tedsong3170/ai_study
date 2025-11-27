package team.song.antigravity_test_01.domain.model

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserTest {

    @Test
    fun shouldCreateUserDomainModelWithAllRequiredFields() {
        // Given
        val id = 1L
        val firebaseUid = "firebase-uid-123"
        val email = "test@example.com"
        val displayName = "Test User"
        val fcmToken = "fcm-token-123"
        val createdAt = LocalDateTime.now()
        val lastLoginAt = LocalDateTime.now()

        // When
        val user = User(
            id = id,
            firebaseUid = firebaseUid,
            email = email,
            displayName = displayName,
            fcmToken = fcmToken,
            createdAt = createdAt,
            lastLoginAt = lastLoginAt
        )

        // Then
        assertNotNull(user)
        assertEquals(id, user.id)
        assertEquals(firebaseUid, user.firebaseUid)
        assertEquals(email, user.email)
        assertEquals(displayName, user.displayName)
        assertEquals(fcmToken, user.fcmToken)
        assertEquals(createdAt, user.createdAt)
        assertEquals(lastLoginAt, user.lastLoginAt)
    }

    @Test
    fun shouldUpdateFcmTokenInUserDomainModel() {
        // Given
        val user = User(
            id = 1L,
            firebaseUid = "firebase-uid-123",
            email = "test@example.com",
            displayName = "Test User",
            fcmToken = null,
            createdAt = LocalDateTime.now(),
            lastLoginAt = LocalDateTime.now()
        )
        val newFcmToken = "new-fcm-token-456"

        // When
        val updatedUser = user.updateFcmToken(newFcmToken)

        // Then
        assertEquals(newFcmToken, updatedUser.fcmToken)
        assertEquals(user.id, updatedUser.id)
        assertEquals(user.firebaseUid, updatedUser.firebaseUid)
    }

    @Test
    fun shouldUpdateLastLoginTimeInUserDomainModel() {
        // Given
        val oldLoginTime = LocalDateTime.now().minusDays(1)
        val user = User(
            id = 1L,
            firebaseUid = "firebase-uid-123",
            email = "test@example.com",
            displayName = "Test User",
            fcmToken = "fcm-token-123",
            createdAt = LocalDateTime.now().minusDays(7),
            lastLoginAt = oldLoginTime
        )
        val newLoginTime = LocalDateTime.now()

        // When
        val updatedUser = user.updateLastLogin(newLoginTime)

        // Then
        assertEquals(newLoginTime, updatedUser.lastLoginAt)
        assertEquals(user.id, updatedUser.id)
        assertEquals(user.firebaseUid, updatedUser.firebaseUid)
        assertEquals(user.fcmToken, updatedUser.fcmToken)
    }
}
