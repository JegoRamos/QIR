package blackpearl.vas_tech.qir

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log

private const val TAG = "AppProvider"
const val CONTENT_AUTHORITY = "blackpearl.vas_tech.qir.provider"

private const val FORMS = 100
private const val FORMS_ID = 101

val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider : ContentProvider() {
    private val uriMatcher by lazy { buildUriMatcher() }

    private fun buildUriMatcher(): UriMatcher {
        Log.d(TAG, "buildUriMatcher starts")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)

        // eg. content://blackpearl.vas_tech.qir.provider/Forms
        matcher.addURI(CONTENT_AUTHORITY, FormsContract.TABLE_NAME, FORMS)

        // eg. content://blackpearl.vas_tech.qir.provider/Forms/1
        matcher.addURI(CONTENT_AUTHORITY, "${FormsContract.TABLE_NAME}/#", FORMS_ID)

        return matcher
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate starts")
        return true
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
             FORMS -> FormsContract.CONTENT_TYPE
             FORMS_ID -> FormsContract.CONTENT_ITEM_TYPE
             else -> throw IllegalArgumentException("Unknown URI: $uri")
         }
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?,
                       sortOrder: String?): Cursor? {

        Log.d(TAG, "query called with URI: $uri")
        val match = uriMatcher.match(uri)
        val queryBuilder = SQLiteQueryBuilder()

        when (match) {
            FORMS -> queryBuilder.tables = FormsContract.TABLE_NAME
            FORMS_ID -> {
                queryBuilder.tables = FormsContract.TABLE_NAME
                val taskId = FormsContract.getId(uri)
                queryBuilder.appendWhere("${FormsContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$taskId")
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        val db = AppDatabase.getInstance(context!!).readableDatabase
        val cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        Log.d(TAG, "query rows returned: ${cursor.count}")
        return cursor
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}