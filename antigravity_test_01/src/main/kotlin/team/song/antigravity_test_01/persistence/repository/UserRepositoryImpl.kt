package team.song.antigravity_test_01.persistence.repository

import org.springframework.stereotype.Repository
import team.song.antigravity_test_01.domain.model.User
import team.song.antigravity_test_01.domain.repository.UserRepository
import team.song.antigravity_test_01.persistence.entity.UserEntity

/**
 * Implementation of domain UserRepository
 * Converts between domain models and JPA entities
 */
@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {

    override fun findByFirebaseUid(firebaseUid: String): User? {
        return userJpaRepository.findByFirebaseUid(firebaseUid)?.toDomain()
    }

    override fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        val savedEntity = userJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
}
