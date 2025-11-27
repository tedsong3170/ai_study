package team.song.antigravity_test_01.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.song.antigravity_test_01.persistence.entity.UserEntity

/**
 * Spring Data JPA repository for UserEntity
 */
interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun findByFirebaseUid(firebaseUid: String): UserEntity?
}
