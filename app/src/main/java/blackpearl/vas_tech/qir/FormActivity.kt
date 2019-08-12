package blackpearl.vas_tech.qir

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_form.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "FormActivity"

// permission codes
private const val IMAGE_PICK_CODE_1 = 1001
private const val IMAGE_PICK_CODE_2 = 1002
private const val IMAGE_PICK_CODE_3 = 1003
private const val IMAGE_PICK_CODE_4 = 1004
private const val CAMERA_CAPTURE_CODE_1 = 1005
private const val CAMERA_CAPTURE_CODE_2 = 1006
private const val CAMERA_CAPTURE_CODE_3 = 1007
private const val CAMERA_CAPTURE_CODE_4 = 1008
private const val GALLERY_PERMISSION_CODE = 1
private const val CAMERA_PERMISSION_CODE = 2
private const val CONTACTS_PERMISSION_CODE = 3

// states
private const val STATE_IMAGE1 = "StateImage1"
private const val STATE_IMAGE2 = "StateImage2"
private const val STATE_IMAGE3 = "StateImage3"
private const val STATE_IMAGE4 = "StateImage4"
private const val STATE_CURRENT_PHOTO_PATH = "MCurrentPhotoPath"

class FormActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {
    // imageUris
    private var image1Uri: String? = null
    private var image2Uri: String? = null
    private var image3Uri: String? = null
    private var image4Uri: String? = null
    private var mCurrentPhotoPath: String? = null

    // limits
    private var PICTURE_SIZE_LIMIT_MB = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        shipmentDateInp.setOnClickListener(this)
        image1.setOnClickListener(this)
        image2.setOnClickListener(this)
        image3.setOnClickListener(this)
        image4.setOnClickListener(this)
        cancelBtn.setOnClickListener(this)

        image1.setOnLongClickListener(this)
        image2.setOnLongClickListener(this)
        image3.setOnLongClickListener(this)
        image4.setOnLongClickListener(this)

        cameraBtn1.setOnClickListener(this)
        cameraBtn2.setOnClickListener(this)
        cameraBtn3.setOnClickListener(this)
        cameraBtn4.setOnClickListener(this)

        // Form (Contact Spinners)
        qirInspectorInp.setOnFocusChangeListener { view, _ ->
            populateContactSpinner(view, qirInspectorInp)
        }

        qirInspectorInp.setOnClickListener {
            populateContactSpinner(it, qirInspectorInp)
        }

        notedByInp.setOnFocusChangeListener { view, _ ->
            populateContactSpinner(view, notedByInp)
        }

        notedByInp.setOnClickListener {
            populateContactSpinner(it, notedByInp)
        }

        approvedByInp.setOnFocusChangeListener { view, _ ->
            populateContactSpinner(view, approvedByInp)
        }

        approvedByInp.setOnClickListener {
            populateContactSpinner(it, approvedByInp)
        }


        // Product Info
        val controlNo = "PETC-QIR/${Calendar.getInstance().get(Calendar.YEAR)}/"
        controlNoInp.setText(controlNo)

        modelInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                // do nothing
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                productCodeInp.text = modelInput.text
                val modelInpLen = modelInput.text.toString().length
                if (modelInpLen > 3) {
                    apInp.setText(modelInput.text.removeRange(0..2).toString())
                    cpInp.setText(modelInput.text.removeRange(0..2).toString())
                    cscInp.setText(modelInput.text.removeRange(0..2).toString())
                }
                if (modelInpLen == 0) {
                    apInp.text.clear()
                    cpInp.text.clear()
                    cscInp.text.clear()
                    productCodeInp.text.clear()
                }

            }
        })

        // shipment date
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "MMMM d, yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
            shipmentDateInp.setText(sdf.format(cal.time))
        }
        shipmentDateInp.setOnClickListener {

            DatePickerDialog(this, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Action Buttons
        saveBtn.setOnClickListener {
            if(saveToDatabase()) {
                finish()
            }
        }

        saveAndCreateBtn.setOnClickListener {
            saveToDatabase()
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onClick(view: View?) {
        when {
            view?.id == R.id.cancelBtn -> finish()
            view is ImageButton -> {
                view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.activate))
                openCamera(view)
            }
            view is ImageView -> {
                view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.activate))
                pickFromGallery(view)
            }
        }
    }

    override fun onLongClick(view: View?): Boolean {
        if (view is ImageView) {
            when (view.id) {
                R.id.image1 -> {
                    image1.setImageResource(R.drawable.image_placeholder)
                    if (image1Uri != null) {
                        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
                    }
                    image1Uri = null
                }
                R.id.image2 -> {
                    image2.setImageResource(R.drawable.image_placeholder)
                    if (image2Uri != null) {
                        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
                    }
                    image2Uri = null
                }
                R.id.image3 -> {
                    image3.setImageResource(R.drawable.image_placeholder)
                    if (image3Uri != null) {
                        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
                    }
                    image3Uri = null
                }
                R.id.image4 -> {
                    image4.setImageResource(R.drawable.image_placeholder)
                    if (image4Uri != null) {
                        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
                    }
                    image4Uri = null
                }
            }
            return true
        }
        return false
    }

    private fun populateContactSpinner(view: View, inpView: AutoCompleteTextView) {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            Snackbar.make(view, "Allow the app to contacts", Snackbar.LENGTH_INDEFINITE)
                .setAction("GRANT") {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS),
                            CONTACTS_PERMISSION_CODE)
                    } else {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", this.packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                }.show()
        } else {
            val contactsArrayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,
                getContacts())
            inpView.setAdapter(contactsArrayAdapter)
        }
    }

    // Contacts
    private fun getContacts(): List<String> {
        val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )
        val contacts = arrayListOf<String>()
        cursor?.use {
            while (it.moveToNext()) {
                contacts.add(it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)))
            }
        }
        return contacts
    }

    // Pictures
    private fun pickFromGallery(imageView: ImageView) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Snackbar.make(scrollView, "Allow the app to access camera and storage", Snackbar.LENGTH_INDEFINITE)
                .setAction("GRANT") {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            GALLERY_PERMISSION_CODE)
                    } else {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", this.packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                }.show()
        } else {
            pickImageFromGallery(imageView)
        }
    }

    private fun openCamera(view: View) {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)  {

            Snackbar.make(scrollView, "Allow the app to access camera and storage", Snackbar.LENGTH_INDEFINITE)
                .setAction("GRANT") {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(this, arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            CAMERA_PERMISSION_CODE)
                    } else {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", this.packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                }.show()
        } else {
            captureImageFromCamera(view)
        }
    }

    private fun pickImageFromGallery(imageView: View) {
        when (imageView.id) {
            R.id.image1 ->  {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_PICK_CODE_1)
            }
            R.id.image2 -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_PICK_CODE_2)
            }
            R.id.image3 -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_PICK_CODE_3)
            }
            R.id.image4 -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_PICK_CODE_4)
            }
        }
    }

    private fun captureImageFromCamera(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()

        val uri: Uri = FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID + ".fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        when (view.id) {
            R.id.cameraBtn1 -> startActivityForResult(intent, CAMERA_CAPTURE_CODE_1)
            R.id.cameraBtn2 -> startActivityForResult(intent, CAMERA_CAPTURE_CODE_2)
            R.id.cameraBtn3 -> startActivityForResult(intent, CAMERA_CAPTURE_CODE_3)
            R.id.cameraBtn4 -> startActivityForResult(intent, CAMERA_CAPTURE_CODE_4)
        }
    }

    // handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // permission from popup granted
        when(requestCode) {
            GALLERY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED)
                Snackbar.make(scrollView, "Permission granted", Snackbar.LENGTH_SHORT).show()
            }
            CAMERA_PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    Snackbar.make(scrollView, "Permission granted", Snackbar.LENGTH_SHORT).show()
            }
            CONTACTS_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED)
                Snackbar.make(scrollView, "Permission granted", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, data.toString())
        if  (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_CODE_1 -> {
                    if (data != null) {
                        // check file size
                        val fileSize: Long = File(getPath(data.data!!)).length()
                        if ((fileSize * 0.000001) >= PICTURE_SIZE_LIMIT_MB) {
                            return Snackbar.make(scrollView, "File size must be less than $PICTURE_SIZE_LIMIT_MB MB", Snackbar.LENGTH_SHORT).show()
                        } else {
                            image1Uri = File(getPath(data.data!!)).toString()
                            image1.setImageURI(image1Uri.toString().toUri())
                        }
                        // image1Uri = data.data.toString()
                       // image1.setImageURI(image1Uri.toString().toUri())
                    }
                }
                IMAGE_PICK_CODE_2 -> {
                    // check file size
                    if (data?.data != null) {
                        val fileSize: Long = File(getPath(data.data!!)).length()
                        if ((fileSize * 0.000001) >= PICTURE_SIZE_LIMIT_MB) {
                            return Snackbar.make(scrollView, "File size must be less than $PICTURE_SIZE_LIMIT_MB MB", Snackbar.LENGTH_SHORT).show()
                        } else {
                            image2Uri = File(getPath(data.data!!)).toString()
                            image2.setImageURI(image2Uri.toString().toUri())
                        }
                    }
                    // image2Uri = data?.data.toString()
                    // image2.setImageURI(image2Uri.toString().toUri())
                }
                IMAGE_PICK_CODE_3 -> {
                    // check file size
                    if (data?.data != null) {
                        val fileSize: Long = File(getPath(data.data!!)).length()
                        if ((fileSize * 0.000001) >= PICTURE_SIZE_LIMIT_MB) {
                            return Snackbar.make(scrollView, "File size must be less than $PICTURE_SIZE_LIMIT_MB MB", Snackbar.LENGTH_SHORT).show()
                        } else {
                            image3Uri = File(getPath(data.data!!)).toString()
                            image3.setImageURI(image3Uri.toString().toUri())
                        }
                    }
                    // image3Uri = data?.data.toString()
                    // image3.setImageURI(image3Uri.toString().toUri())
                }
                IMAGE_PICK_CODE_4 ->  {
                    // check file size
                    if (data?.data != null) {
                        val fileSize: Long = File(getPath(data.data!!)).length()
                        if ((fileSize * 0.000001) >= PICTURE_SIZE_LIMIT_MB) {
                            return Snackbar.make(scrollView, "File size must be less than $PICTURE_SIZE_LIMIT_MB MB", Snackbar.LENGTH_SHORT).show()
                        } else {
                            image4Uri = File(getPath(data.data!!)).toString()
                            image4.setImageURI(image4Uri.toString().toUri())
                        }
                    }
                    // image4Uri = data?.data.toString()
                    // image4.setImageURI(image4Uri.toString().toUri())
                }
                CAMERA_CAPTURE_CODE_1 -> {
                    Log.i(TAG, "onActivityResults: ${mCurrentPhotoPath.toString()}")
                    val auxFile = File(mCurrentPhotoPath!!)
                    image1Uri = auxFile.toUri().toString()

                    // check file size
                    val fileSize: Long = auxFile.length()
                    if ((fileSize * 0.000001) > PICTURE_SIZE_LIMIT_MB) {
                        return Snackbar.make(scrollView, "Image size must be less than $PICTURE_SIZE_LIMIT_MB MB", Snackbar.LENGTH_SHORT).show()
                    } else {
                        image1.setImageURI(image1Uri.toString().toUri())
                    }

                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = (image1Uri.toString().toUri())
                    sendBroadcast(mediaScanIntent)
                }
                CAMERA_CAPTURE_CODE_2 -> {
                    val auxFile = File(mCurrentPhotoPath!!)
                    image2Uri = auxFile.toUri().toString()

                    // check file size
                    val fileSize: Long = auxFile.length()
                    if ((fileSize * 0.000001) > PICTURE_SIZE_LIMIT_MB) {
                        return Snackbar.make(scrollView, "Image size must be less than $PICTURE_SIZE_LIMIT_MB MB", Snackbar.LENGTH_SHORT).show()
                    } else {
                        image2.setImageURI(image2Uri.toString().toUri())
                    }

                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = (image2Uri.toString().toUri())
                    sendBroadcast(mediaScanIntent)
                }
                CAMERA_CAPTURE_CODE_3 -> {
                    val auxFile = File(mCurrentPhotoPath!!)
                    image3Uri = auxFile.toUri().toString()

                    // check file size
                    val fileSize: Long = auxFile.length()
                    if ((fileSize * 0.000001) > PICTURE_SIZE_LIMIT_MB) {
                        return Snackbar.make(scrollView, "Image size must be less than $PICTURE_SIZE_LIMIT_MB MB", Snackbar.LENGTH_SHORT).show()
                    } else {
                        image3.setImageURI(image3Uri.toString().toUri())
                    }

                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = (image3Uri.toString().toUri())
                    sendBroadcast(mediaScanIntent)
                }
                CAMERA_CAPTURE_CODE_4 -> {
                    val auxFile = File(mCurrentPhotoPath!!)
                    image4Uri = auxFile.toUri().toString()

                    // check file size
                    val fileSize: Long = auxFile.length()
                    if ((fileSize * 0.000001) > PICTURE_SIZE_LIMIT_MB) {
                        return Snackbar.make(scrollView, "Image size must be less than $PICTURE_SIZE_LIMIT_MB MB", Snackbar.LENGTH_SHORT).show()
                    } else {
                        image4.setImageURI(image4Uri.toString().toUri())
                    }

                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = (image4Uri.toString().toUri())
                    sendBroadcast(mediaScanIntent)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(
            getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ), "Camera"
        )
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    private fun getPath(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(uri, projection, null, null, null)
        startManagingCursor(cursor)
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }

    private fun saveToDatabase(): Boolean {
        val values = ContentValues().apply {
            // QIR Inspector
            if (!qirInspectorInp.text.isNullOrEmpty() && qirInspectorInp.text.isNotBlank()) {
                put(FormsContract.Columns.QIR_INSPECTOR, qirInspectorInp.text.toString())
            }
            else {
                Snackbar.make(qirInspectorInp, "QIR Inspector field required", Snackbar.LENGTH_SHORT).show()
                return false
            }
            // Control Number
            put(FormsContract.Columns.CONTROL_NUMBER, controlNoInp.text.toString())
            // Network
            put(FormsContract.Columns.NETWORK, networkInp.text.toString())
            // Model
            if (!modelInput.text.isNullOrEmpty() && modelInput.text.isNotBlank()) {
                put(FormsContract.Columns.MODEL, modelInput.text.toString())
            }
            else {
                Snackbar.make(modelInput, "Model field required", Snackbar.LENGTH_SHORT).show()
                return false
            }
            // Product Code
            if (!productCodeInp.text.isNullOrEmpty() && productCodeInp.text.isNotBlank()) {
                put(FormsContract.Columns.PRODUCT_CODE, productCodeInp.text.toString())
            }
            else {
                Snackbar.make(productCodeInp, "Product Code field required", Snackbar.LENGTH_SHORT).show()
                return false
            }
            // IMEI
            put(FormsContract.Columns.IMEI, imeiInp.text.toString())
            // Serial Number
            put(FormsContract.Columns.SERIAL_NUMBER, serialNoInp.text.toString())
            // Plant/Origin
            put(FormsContract.Columns.PLANT_ORIGIN, plantOriginInp.text.toString())
            // Shipment Date
            put(FormsContract.Columns.SHIPMENT_DATE, shipmentDateInp.text.toString())
            // AP
            put(FormsContract.Columns.AP, apInp.text.toString())
            // CP
            put(FormsContract.Columns.CP, cpInp.text.toString())
            // CSC
            put(FormsContract.Columns.CSC, cscInp.text.toString())
            // HW Version
            put(FormsContract.Columns.HW_VERSION, hwVersionInp.text.toString())
            // Defect Details
            put(FormsContract.Columns.DEFECT_DETAILS, defectDetailsInp.text.toString())
            // Image1
            put(FormsContract.Columns.IMAGE_1, image1Uri)
            // Image2
            put(FormsContract.Columns.IMAGE_2, image2Uri)
            // Image3
            put(FormsContract.Columns.IMAGE_3, image3Uri)
            // Image4
            put(FormsContract.Columns.IMAGE_4, image4Uri)
            // Noted By
            put(FormsContract.Columns.NOTED_BY, notedByInp.text.toString())
            // Approved By
            put(FormsContract.Columns.APPROVED_BY, approvedByInp.text.toString())
            // Date Created
            val date = Date()
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val dateFormatted: String = formatter.format(date)
            put(FormsContract.Columns.DATE_CREATED, dateFormatted)
            // isActive
            put(FormsContract.Columns.IS_ACTIVE, 1)
            // isRemoved
            put(FormsContract.Columns.IS_REMOVED, 0)
        }
        val uri = contentResolver.insert(FormsContract.CONTENT_URI, values)
        Log.d(TAG,"New row id (in uri) is $uri")
        Log.d(TAG, "id: ${FormsContract.getId(uri!!)}")
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSaveInstanceState: $image1Uri")
        outState.putString(STATE_IMAGE1, image1Uri)
        outState.putString(STATE_IMAGE2, image2Uri)
        outState.putString(STATE_IMAGE3, image3Uri)
        outState.putString(STATE_IMAGE4, image4Uri)
        outState.putString(STATE_CURRENT_PHOTO_PATH, mCurrentPhotoPath)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        image1Uri = savedInstanceState.getString(STATE_IMAGE1)
        image2Uri = savedInstanceState.getString(STATE_IMAGE2)
        image3Uri = savedInstanceState.getString(STATE_IMAGE3)
        image4Uri = savedInstanceState.getString(STATE_IMAGE4)
        mCurrentPhotoPath = savedInstanceState.getString(STATE_CURRENT_PHOTO_PATH)

        image1.setImageURI(image1Uri?.toUri())
        image2.setImageURI(image2Uri?.toUri())
        image3.setImageURI(image3Uri?.toUri())
        image4.setImageURI(image4Uri?.toUri())
    }

}
