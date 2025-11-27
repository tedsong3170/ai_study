package team.song.antigravity_test_01.domain.model

import java.time.LocalDateTime

/**
 * Pure domain model for User
 * Framework-independent, contains business logic
 */
data class User(
    val id: Long?,
    val firebaseUid: String,
    val email: String,
    val displayName: String?,
    val fcmToken: String?,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime
) {
    /**
     * Updates the FCM token for this user
     * Returns a new User instance with the updated token
     */
    fun updateFcmToken(newToken: String): User {
        return this.copy(fcmToken = newToken)
    }

    /**
     * Updates the last login time for this user
     * Returns a new User instance with the updated last login time
     */
    fun updateLastLogin(loginTime: LocalDateTime): User {
        return this.copy(lastLoginAt = loginTime)
    }
}
