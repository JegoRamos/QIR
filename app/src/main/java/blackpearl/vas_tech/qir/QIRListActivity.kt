package blackpearl.vas_tech.qir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_qir_list.*

class QIRListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qir_list)

        addBtn.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Use the RECENT KEY to close the app", Toast.LENGTH_SHORT).show()
        return
    }
}
