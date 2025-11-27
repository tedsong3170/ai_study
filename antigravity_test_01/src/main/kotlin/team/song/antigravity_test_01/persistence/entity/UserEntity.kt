package team.song.antigravity_test_01.persistence.entity

import jakarta.persistence.*
import team.song.antigravity_test_01.domain.model.User
import java.time.LocalDateTime

/**
 * JPA Entity for User
 * Maps to database table with JPA annotations
 */
@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    val firebaseUid: String,

    @Column(nullable = false, length = 255)
    val email: String,

    @Column(length = 255)
    val displayName: String? = null,

    @Column(length = 500)
    val fcmToken: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val lastLoginAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Converts this entity to a domain model
     */
    fun toDomain(): User {
        return User(
            id = this.id,
            firebaseUid = this.firebaseUid,
            email = this.email,
            displayName = this.displayName,
            fcmToken = this.fcmToken,
            createdAt = this.createdAt,
            lastLoginAt = this.lastLoginAt
        )
    }

    companion object {
        /**
         * Creates an entity from a domain model
         */
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                firebaseUid = user.firebaseUid,
                email = user.email,
                displayName = user.displayName,
                fcmToken = user.fcmToken,
                createdAt = user.createdAt,
                lastLoginAt = user.lastLoginAt
            )
        }
    }
}
