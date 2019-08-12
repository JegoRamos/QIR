package blackpearl.vas_tech.qir

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val TAG = "AppDatabase"
private const val DATABASE_NAME = "QIR.db"
private const val DATABASE_VERSION = 3

internal class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "onCreate: starts")
        val sSQL = """
            CREATE TABLE ${FormsContract.TABLE_NAME}(
            ${FormsContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${FormsContract.Columns.QIR_INSPECTOR} TEXT NOT NULL,
            ${FormsContract.Columns.CONTROL_NUMBER} TEXT,
            ${FormsContract.Columns.NETWORK} TEXT,
            ${FormsContract.Columns.MODEL} TEXT,
            ${FormsContract.Columns.PRODUCT_CODE} TEXT,
            ${FormsContract.Columns.IMEI} TEXT,
            ${FormsContract.Columns.SERIAL_NUMBER} TEXT,
            ${FormsContract.Columns.PLANT_ORIGIN} TEXT,
            ${FormsContract.Columns.SHIPMENT_DATE} TEXT,
            ${FormsContract.Columns.AP} TEXT,
            ${FormsContract.Columns.CP} TEXT,
            ${FormsContract.Columns.CSC} TEXT,
            ${FormsContract.Columns.HW_VERSION} TEXT,
            ${FormsContract.Columns.DEFECT_DETAILS} TEXT,
            ${FormsContract.Columns.IMAGE_1} TEXT,
            ${FormsContract.Columns.IMAGE_2} TEXT,
            ${FormsContract.Columns.IMAGE_3} TEXT,
            ${FormsContract.Columns.IMAGE_4} TEXT,
            ${FormsContract.Columns.NOTED_BY} TEXT,
            ${FormsContract.Columns.APPROVED_BY} TEXT,
            ${FormsContract.Columns.IS_ACTIVE} BOOLEAN NOT NULL,
            ${FormsContract.Columns.IS_REMOVED} BOOLEAN NOT NULL
            )
        """.trimIndent()
        Log.d(TAG, sSQL)

        if (db != null) {
            db.execSQL(sSQL)
        } else {
            Log.e(TAG,"Database not created")
            throw SQLiteException("Error creating database")
        }

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)

}