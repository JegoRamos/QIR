package blackpearl.vas_tech.qir

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

object FormsContract {
    internal const val TABLE_NAME = "Forms"

    // The URI to access the Forms Table
    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    // Form fields
    object Columns {
        const val ID = BaseColumns._ID
        const val QIR_INSPECTOR = "QIRInspector"
        const val CONTROL_NUMBER = "ControlNumber"
        const val NETWORK = "Network"
        const val MODEL = "Model"
        const val PRODUCT_CODE = "ProductCode"
        const val IMEI = "IMEI"
        const val SERIAL_NUMBER = "SerialNumber"
        const val PLANT_ORIGIN = "PlantOrigin"
        const val SHIPMENT_DATE = "ShipmentDate"
        const val AP = "AP"
        const val CP = "CP"
        const val CSC = "CSC"
        const val HW_VERSION = "HWVersion"
        const val DEFECT_DETAILS = "DefectDetails"
        const val IMAGE_1 = "Image1"
        const val IMAGE_2 = "Image2"
        const val IMAGE_3 = "Image3"
        const val IMAGE_4 = "Image4"
        const val NOTED_BY = "NotedBy"
        const val APPROVED_BY = "ApprovedBy"
        const val IS_ACTIVE = "IsActive"
        const val IS_REMOVED = "IsRemoved"
    }

    fun getId(uri: Uri): Long {
        return ContentUris.parseId(uri)
    }

    fun buildUriFromId(id: Long): Uri {
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }
}