package blackpearl.vas_tech.qir

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_qir_list.*

private const val TAG = "QIRListActivity"
class QIRListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qir_list)

        val cursor = contentResolver.query(FormsContract.buildUriFromId(0),
            null,
            null,
            null,
            null
            )

        if (cursor != null) {
            Log.d(TAG, "*****************************************************************************************")
            cursor.use {
                while (it.moveToNext()) {
                    with (it) {
                        val id = getLong(0)
                        val qirInspector = getString(1)
                        val controlNumber = getString(2)
                        val network = getString(3)
                        val model = getString(4)
                        val productCode = getString(5)
                        val imei = getString(6)
                        val serialNumber = getString(7)
                        val plantOrigin = getString(8)
                        val shipmentDate = getString(9)
                        val ap = getString(10)
                        val cp = getString(11)
                        val csc = getString(12)
                        val hwVersion = getString(13)
                        val defectDetails = getString(14)
                        val image1 = getString(15)
                        val image2 = getString(16)
                        val image3 = getString(17)
                        val image4 = getString(18)
                        val isActive = getString(19)?.toBoolean()
                        val isRemoved = getString(20)?.toBoolean()
                        Log.d(TAG, """ID: $id, 
                            QIRInspector: $qirInspector, 
                            controlNumber: $controlNumber,
                            network: $network
                            model: $model
                            productCode: $productCode
                            imei: $imei
                            serialNumber: $serialNumber
                            plantOrigin: $plantOrigin
                            shipmentDate: $shipmentDate
                            ap: $ap
                            cp: $cp
                            csc: $csc
                            hwVersion: $hwVersion
                            defectDetails: $defectDetails
                            image1: $image1
                            image2: $image2
                            image3: $image3
                            image4: $image4
                            isActive: $isActive
                            isRemoved: $isRemoved
                            """
                            .trimIndent())
                        Log.d(TAG, "**************************************************************************************8")
                    }
                }
            }
        }

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
