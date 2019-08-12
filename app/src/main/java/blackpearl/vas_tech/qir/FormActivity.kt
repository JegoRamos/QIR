package blackpearl.vas_tech.qir

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_form.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FormActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {
    private val TAG = "FormActivity"

    // permission codes
    private val IMAGE_PICK_CODE_1 = 1001
    private val IMAGE_PICK_CODE_2 = 1002
    private val IMAGE_PICK_CODE_3 = 1003
    private val IMAGE_PICK_CODE_4 = 1004
    private val CAMERA_CAPTURE_CODE_1 = 1005
    private val CAMERA_CAPTURE_CODE_2 = 1006
    private val CAMERA_CAPTURE_CODE_3 = 1007
    private val CAMERA_CAPTURE_CODE_4 = 1008
    private val GALLERY_PERMISSION_CODE = 1
    private val CAMERA_PERMISSION_CODE = 2

    // states
    private val STATE_IMAGE1 = "StateImage1"
    private val STATE_IMAGE2 = "StateImage2"
    private val STATE_IMAGE3 = "StateImage3"
    private val STATE_IMAGE4 = "StateImage4"
    private val STATE_CURRENT_PHOTO_PATH = "MCurrentPhotoPath"

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
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
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

    // Pictures
    private fun pickFromGallery(imageView: ImageView) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_DENIED) {
            //permission denied
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            //show popup to request runtime permission
            requestPermissions(permissions, GALLERY_PERMISSION_CODE)
        }
        else {
            //permission already granted
            pickImageFromGallery(imageView)
        }
    }

    private fun openCamera(view: View) {
        if (checkSelfPermission(Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_DENIED && checkSelfPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            // permission denied
            val permissions = arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permissions, CAMERA_PERMISSION_CODE)

        } else if (checkSelfPermission(Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_DENIED && checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // permission denied
            val permissions = arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permissions, CAMERA_PERMISSION_CODE)
        } else if (checkSelfPermission(Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            // permission denied
            val permissions = arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permissions, CAMERA_PERMISSION_CODE)
        } else {
            // permission granted already
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
            GALLERY_PERMISSION_CODE -> if (grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) // this.pickImageFromGallery()
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()

            CAMERA_PERMISSION_CODE -> if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()

            else -> {
                // permission from popup denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
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
                            return Toast.makeText(this, "File size must be less than $PICTURE_SIZE_LIMIT_MB MB", Toast.LENGTH_SHORT).show()
                        }
                        image1Uri = data.data.toString()
                        image1.setImageURI(image1Uri.toString().toUri())
                    }
                }
                IMAGE_PICK_CODE_2 -> {
                    // check file size
                    if (data?.data != null) {
                        val fileSize: Long = File(getPath(data.data!!)).length()
                        if ((fileSize * 0.000001) >= PICTURE_SIZE_LIMIT_MB) {
                            return Toast.makeText(this, "File size must be less than $PICTURE_SIZE_LIMIT_MB MB", Toast.LENGTH_SHORT).show()
                        }
                    }
                    image2Uri = data?.data.toString()
                    image2.setImageURI(image2Uri.toString().toUri())
                }
                IMAGE_PICK_CODE_3 -> {
                    // check file size
                    if (data?.data != null) {
                        val fileSize: Long = File(getPath(data.data!!)).length()
                        if ((fileSize * 0.000001) >= PICTURE_SIZE_LIMIT_MB) {
                            return Toast.makeText(this, "File size must be less than $PICTURE_SIZE_LIMIT_MB MB", Toast.LENGTH_SHORT).show()
                        }
                    }
                    image3Uri = data?.data.toString()
                    image3.setImageURI(image3Uri.toString().toUri())
                }
                IMAGE_PICK_CODE_4 ->  {
                    // check file size
                    if (data?.data != null) {
                        val fileSize: Long = File(getPath(data.data!!)).length()
                        if ((fileSize * 0.000001) >= PICTURE_SIZE_LIMIT_MB) {
                            return Toast.makeText(this, "File size must be less than $PICTURE_SIZE_LIMIT_MB MB", Toast.LENGTH_SHORT).show()
                        }
                    }
                    image4Uri = data?.data.toString()
                    image4.setImageURI(image4Uri.toString().toUri())
                }
                CAMERA_CAPTURE_CODE_1 -> {
                    Log.i(TAG, "onActivityResults: ${mCurrentPhotoPath.toString()}")
                    val auxFile = File(mCurrentPhotoPath!!)
                    image1Uri = auxFile.toUri().toString()

                    // check file size
                    val fileSize: Long = auxFile.length()
                    if ((fileSize * 0.000001) > PICTURE_SIZE_LIMIT_MB) {
                        Toast.makeText(this, "Image must be less than $PICTURE_SIZE_LIMIT_MB MB", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this, "Image must be less than $PICTURE_SIZE_LIMIT_MB MB", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this, "Image must be less than $PICTURE_SIZE_LIMIT_MB MB", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this, "Image must be less than $PICTURE_SIZE_LIMIT_MB MB", Toast.LENGTH_SHORT).show()
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
