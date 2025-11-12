package hk.hku.cs.swifttrip

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Immediately start the real first screen (SignInActivity)
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}
