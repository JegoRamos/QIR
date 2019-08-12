package blackpearl.vas_tech.qir

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import blackpearl.vas_tech.qir.utils.EXTRA_AUTH_ERROR
import kotlinx.android.synthetic.main.activity_auth_error.*

class AuthErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_error)

        val error = intent.getStringExtra(EXTRA_AUTH_ERROR)
        errorTxt.text = error

        closeBtn.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }
    }
}
