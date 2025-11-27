package team.song.antigravity_test_01.service

import org.springframework.stereotype.Service
import team.song.antigravity_test_01.domain.repository.UserRepository

/**
 * FCM service
 * Handles FCM token registration
 */
@Service
class FcmService(
    private val userRepository: UserRepository
) {

    /**
     * Registers FCM token for a user identified by Firebase UID
     */
    fun registerFcmToken(firebaseUid: String, fcmToken: String) {
        val user = userRepository.findByFirebaseUid(firebaseUid)
            ?: throw IllegalArgumentException("User not found")

        val updatedUser = user.updateFcmToken(fcmToken)
        userRepository.save(updatedUser)
    }
}
