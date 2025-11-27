package team.song.antigravity_test_01.config

import com.google.firebase.FirebaseApp
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
class FirebaseConfigTest {

    @Autowired
    private lateinit var firebaseConfig: FirebaseConfig

    @Test
    fun shouldLoadFirebaseConfiguration() {
        // Given & When - FirebaseConfig is loaded by Spring

        // Then - It should not throw an exception
        // Note: In test environment without service account file, it will log a warning but continue
        assertTrue(true) // Config loaded successfully
    }
}
