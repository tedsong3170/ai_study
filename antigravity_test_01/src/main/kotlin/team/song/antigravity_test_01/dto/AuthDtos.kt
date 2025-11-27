package team.song.antigravity_test_01.dto

/**
 * Data Transfer Objects for Authentication
 */

data class LoginRequest(
    val firebaseToken: String
)

data class UserDto(
    val id: Long?,
    val firebaseUid: String,
    val email: String,
    val displayName: String?,
    val fcmToken: String?
)

data class LoginResponse(
    val user: UserDto,
    val message: String = "Login successful"
)
