package blackpearl.vas_tech.qir

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
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
        Log.d(TAG, "query match is $match")
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

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(TAG, "insert called with URI: $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "insert match is $match")

        val recordId: Long
        val returnUri: Uri

        when (match) {
            FORMS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                recordId = db.insert(FormsContract.TABLE_NAME, null, values)
                if (recordId != -1L) {
                    returnUri = FormsContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert. Uri was invalid: $uri")
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        Log.d(TAG, "Exiting insert: returnUri: $returnUri")
        return returnUri
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "insert called with URI: $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "insert match is $match")

        val count: Int
        var selectionCriteria: String

        when (match) {
            FORMS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.update(FormsContract.TABLE_NAME, values, selection, selectionArgs)
            }
            FORMS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = FormsContract.getId(uri)
                selectionCriteria = "${FormsContract.Columns.ID} = $id"

                if (!selection.isNullOrEmpty()) {
                    selectionCriteria += " AND $selection"
                }
                count = db.update(FormsContract.TABLE_NAME, values, selection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        Log.d(TAG,"Exiting update, returning count $count")
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "delete called with URI: $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "delete match is $match")

        val count: Int
        var selectionCriteria: String

        when (match) {
            FORMS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.delete(FormsContract.TABLE_NAME, selection, selectionArgs)
            }
            FORMS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = FormsContract.getId(uri)
                selectionCriteria = "${FormsContract.Columns.ID} = $id"

                if (!selection.isNullOrEmpty()) {
                    selectionCriteria += " AND $selection"
                }
                count = db.delete(FormsContract.TABLE_NAME, selection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        Log.d(TAG,"Exiting delete, returning count $count")
        return count
    }

}