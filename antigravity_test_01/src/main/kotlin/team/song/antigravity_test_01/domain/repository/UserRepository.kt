package team.song.antigravity_test_01.domain.repository

import team.song.antigravity_test_01.domain.model.User

/**
 * Domain repository interface for User
 * Framework-independent contract
 */
interface UserRepository {
    fun findByFirebaseUid(firebaseUid: String): User?
    fun save(user: User): User
}
