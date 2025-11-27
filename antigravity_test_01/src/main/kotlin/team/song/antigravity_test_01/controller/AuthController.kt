package team.song.antigravity_test_01.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import team.song.antigravity_test_01.dto.LoginRequest
import team.song.antigravity_test_01.dto.LoginResponse
import team.song.antigravity_test_01.service.AuthService

/**
 * Authentication REST Controller
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest?): ResponseEntity<Any> {
        // Validate request
        if (request == null || request.firebaseToken.isBlank()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "Firebase token is required"))
        }

        return try {
            val userDto = authService.verifyTokenAndGetUser(request.firebaseToken)
            ResponseEntity.ok(LoginResponse(user = userDto))
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Invalid Firebase token"))
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Internal server error"))
        }
    }
}
