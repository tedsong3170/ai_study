package team.song.antigravity_test_01.persistence.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import team.song.antigravity_test_01.domain.model.User
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
@Import(UserRepositoryImpl::class)
class UserRepositoryImplTest {

    @Autowired
    private lateinit var userRepository: UserRepositoryImpl

    @Test
    fun shouldSaveAndFindUserByFirebaseUid() {
        // Given
        val user = User(
            id = null,
            firebaseUid = "firebase-uid-test-123",
            email = "test@example.com",
            displayName = "Test User",
            fcmToken = null,
            createdAt = LocalDateTime.now(),
            lastLoginAt = LocalDateTime.now()
        )

        // When
        val savedUser = userRepository.save(user)
        val foundUser = userRepository.findByFirebaseUid("firebase-uid-test-123")

        // Then
        assertNotNull(savedUser.id)
        assertNotNull(foundUser)
        assertEquals(savedUser.firebaseUid, foundUser.firebaseUid)
        assertEquals(savedUser.email, foundUser.email)
        assertEquals(savedUser.displayName, foundUser.displayName)
    }

    @Test
    fun shouldReturnNullWhenUserNotFound() {
        // When
        val foundUser = userRepository.findByFirebaseUid("non-existent-uid")

        // Then
        assertNull(foundUser)
    }
}
