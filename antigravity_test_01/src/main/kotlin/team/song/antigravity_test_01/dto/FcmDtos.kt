package team.song.antigravity_test_01.dto

/**
 * Data Transfer Objects for FCM
 */

data class FcmTokenRequest(
    val fcmToken: String
)

data class FcmTokenResponse(
    val message: String = "FCM token registered successfully"
)
