package org.invokevirtual.launcher.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsPickerSessionContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Privacy-first Android 17 Contacts Picker integration (`ContactsPickerSessionContract.ACTION_PICK_CONTACTS`)
 * with automatic backward-compatible `ACTION_PICK` fallback on API < 37.
 *
 * Requires zero `READ_CONTACTS` or `CALL_PHONE` permissions.
 */
object ContactsPickerHelper {

    private const val SELECTION_LIMIT = 8

    fun isAndroid17PickerSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN) return false
        val intent = Intent(ContactsPickerSessionContract.ACTION_PICK_CONTACTS)
        return intent.resolveActivity(context.packageManager) != null
    }

    /**
     * Builds the Contacts Picker intent requesting only `Phone.CONTENT_ITEM_TYPE` so the system
     * picker automatically filters to callable/textable contacts.
     */
    fun createPickerIntent(context: Context): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            val modern = Intent(ContactsPickerSessionContract.ACTION_PICK_CONTACTS).apply {
                putStringArrayListExtra(
                    ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,
                    arrayListOf(Phone.CONTENT_ITEM_TYPE),
                )
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                putExtra(ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_SELECTION_LIMIT, SELECTION_LIMIT)
                putExtra(ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_MATCH_ALL_DATA_FIELDS, false)
            }
            if (modern.resolveActivity(context.packageManager) != null) {
                return modern
            }
        }
        return Intent(Intent.ACTION_PICK, Phone.CONTENT_URI)
    }

    /**
     * Reads the one-time granted result URI on [Dispatchers.IO].
     * Per Android 17 Session URI rules, never passes `selection` or `selectionArgs`.
     */
    suspend fun parsePickerResult(
        context: Context,
        resultUri: Uri,
    ): List<QuickContact> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
        )
        aggregated.values.toList()
    }
}
