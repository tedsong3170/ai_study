package team.song.antigravity_test_01.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import team.song.antigravity_test_01.dto.FcmTokenRequest
import team.song.antigravity_test_01.dto.FcmTokenResponse
import team.song.antigravity_test_01.service.AuthService
import team.song.antigravity_test_01.service.FcmService

/**
 * FCM Token REST Controller
 */
@RestController
@RequestMapping("/api/fcm")
class FcmController(
    private val fcmService: FcmService,
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun registerFcmToken(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestBody request: FcmTokenRequest?
    ): ResponseEntity<Any> {
        // Validate FCM token in request
        if (request == null || request.fcmToken.isBlank()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "FCM token is required"))
        }

        // Validate Authorization header
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Authorization required"))
        }

        val firebaseToken = authHeader.substring(7) // Remove "Bearer " prefix

        return try {
            // Verify Firebase token and get user
            val userDto = authService.verifyTokenAndGetUser(firebaseToken)
            
            // Register FCM token
            fcmService.registerFcmToken(userDto.firebaseUid, request.fcmToken)
            
            ResponseEntity.ok(FcmTokenResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Invalid authorization"))
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Internal server error"))
        }
    }
}
