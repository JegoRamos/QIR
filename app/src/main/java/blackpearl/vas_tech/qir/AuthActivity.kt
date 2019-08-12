package blackpearl.vas_tech.qir

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.FragmentActivity
import blackpearl.vas_tech.qir.utils.EXTRA_AUTH_ERROR
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_auth.*
import java.util.concurrent.Executors
import kotlin.system.exitProcess


class AuthActivity : AppCompatActivity() {
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)


        val executor = Executors.newSingleThreadExecutor()
        val activity: FragmentActivity = this // reference to activity

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when(errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        // Do nothing
                    }
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        // Do nothing
                    }
                    BiometricPrompt.ERROR_CANCELED -> {
                        // Do nothing
                    }
                    BiometricPrompt.ERROR_LOCKOUT -> {
                        val errorTxt = "The app is temporarily locked. Please try again later."
                        Snackbar.make(fingerPrintImg, errorTxt, Snackbar.LENGTH_LONG).show()
                    }
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        val errorTxt = "The app is permanently locked. Re-enable by entering the " +
                                "PIN or PATTERN."
                        Snackbar.make(fingerPrintImg, errorTxt, Snackbar.LENGTH_LONG).show()
                    }
                    else -> {
                        Snackbar.make(fingerPrintImg, errString, Snackbar.LENGTH_LONG).show()
                    }
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Called when a biometric is recognized.
                val intent = Intent(activity, QIRListActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Called when a biometric is valid but not recognized.
                // Just logging. Letting the default behaviour do its job
                Log.d("MainActivity", "Don't know you")
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Gently place your finger on the scanner")
            .setDescription("It's either placed at the Home button or at the back of the phone")
            .setNegativeButtonText("CANCEL")
            .build()

        fun isAvailable(context: Context): Boolean {
            val fingerprintManager = FingerprintManagerCompat.from(context)
            return fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()
        }

        fingerPrintImg.setOnClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.activate))
            if (isAvailable(this)) {
                biometricPrompt.authenticate(promptInfo)
            } else {
                Snackbar.make(fingerPrintImg, "No fingerprints enrolled or Biometrics may not be supported by the device",
                    Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        exitProcess(0)
    }
}
