package team.song.antigravity_test_01.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import org.springframework.stereotype.Service
import team.song.antigravity_test_01.domain.model.User
import team.song.antigravity_test_01.domain.repository.UserRepository
import team.song.antigravity_test_01.dto.UserDto
import java.time.LocalDateTime

/**
 * Authentication service
 * Handles Firebase token verification and user management
 */
@Service
class AuthService(
    private val userRepository: UserRepository
) {

    /**
     * Verifies Firebase token and returns user information
     * Creates new user if doesn't exist, updates last login if exists
     */
    fun verifyTokenAndGetUser(firebaseToken: String): UserDto {
        val decodedToken = verifyFirebaseToken(firebaseToken)
        val firebaseUid = decodedToken.uid
        val email = decodedToken.email ?: throw IllegalArgumentException("Email not found in token")
        val displayName = decodedToken.name

        // Find or create user
        val existingUser = userRepository.findByFirebaseUid(firebaseUid)
        val user = if (existingUser != null) {
            // Update last login time
            val updatedUser = existingUser.updateLastLogin(LocalDateTime.now())
            userRepository.save(updatedUser)
        } else {
            // Create new user
            val newUser = User(
                id = null,
                firebaseUid = firebaseUid,
                email = email,
                displayName = displayName,
                fcmToken = null,
                createdAt = LocalDateTime.now(),
                lastLoginAt = LocalDateTime.now()
            )
            userRepository.save(newUser)
        }

        return user.toDto()
    }

    private fun verifyFirebaseToken(token: String): FirebaseToken {
        return try {
            FirebaseAuth.getInstance().verifyIdToken(token)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Firebase token: ${e.message}")
        }
    }

    private fun User.toDto(): UserDto {
        return UserDto(
            id = this.id,
            firebaseUid = this.firebaseUid,
            email = this.email,
            displayName = this.displayName,
            fcmToken = this.fcmToken
        )
    }
}
