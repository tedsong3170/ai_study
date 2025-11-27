package team.song.antigravity_test_01.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import jakarta.annotation.PostConstruct

/**
 * Firebase configuration
 * Initializes Firebase Admin SDK on application startup
 */
@Configuration
class FirebaseConfig {

    @Value("\${firebase.service-account-file:}")
    private lateinit var serviceAccountFile: Resource

    @PostConstruct
    fun initialize() {
        // Skip initialization if already initialized (for tests)
        if (FirebaseApp.getApps().isNotEmpty()) {
            return
        }

        try {
            // Check if service account file exists
            if (!serviceAccountFile.exists()) {
                println("WARNING: Firebase service account file not found. Firebase features will not work.")
                return
            }

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountFile.inputStream))
                .build()

            FirebaseApp.initializeApp(options)
            println("Firebase initialized successfully")
        } catch (e: Exception) {
            println("WARNING: Failed to initialize Firebase: ${e.message}")
            // Don't throw exception to allow app to start without Firebase in development
        }
    }
}
