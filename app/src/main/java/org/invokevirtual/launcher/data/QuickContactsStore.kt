package org.invokevirtual.launcher.data

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.quickContactsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "invoke_quick_contacts",
)

/**
 * Privacy-minimal contact snapshot returned from Android 17's Contacts Picker.
 * Persisted immediately because Session URIs grant temporary one-time read access only.
 */
@Immutable
data class QuickContact(
    val lookupKey: String,
    val displayName: String,
    val phoneNumber: String,
) {
    val initials: String
        get() = displayName
            .trim()
            .split(' ')
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "#" }
}

/**
 * Persists user-selected Quick People in DataStore without ever requesting `READ_CONTACTS`.
 */
class QuickContactsStore(private val context: Context) {
    private object Keys {
        val CONTACTS = stringPreferencesKey("quick_contacts_v1")
    }

    val contacts: Flow<List<QuickContact>> = context.quickContactsDataStore.data.map { prefs ->
        decodeQuickContacts(prefs[Keys.CONTACTS])
    }

    suspend fun upsertContacts(picked: List<QuickContact>) {
        if (picked.isEmpty()) return
        context.quickContactsDataStore.edit { prefs ->
            val current = decodeQuickContacts(prefs[Keys.CONTACTS]).toMutableList()
            for (contact in picked) {
                val existingIdx = current.indexOfFirst { it.lookupKey == contact.lookupKey }
                if (existingIdx >= 0) {
                    current[existingIdx] = contact
                } else {
                    current += contact
                }
            }
            prefs[Keys.CONTACTS] = encodeQuickContacts(current.take(MAX_QUICK_CONTACTS))
        }
    }

    suspend fun removeContact(lookupKey: String) {
        context.quickContactsDataStore.edit { prefs ->
            val updated = decodeQuickContacts(prefs[Keys.CONTACTS])
                .filterNot { it.lookupKey == lookupKey }
            if (updated.isEmpty()) {
                prefs.remove(Keys.CONTACTS)
            } else {
                prefs[Keys.CONTACTS] = encodeQuickContacts(updated)
            }
        }
    }

    companion object {
        const val MAX_QUICK_CONTACTS = 12
        private const val RECORD_SEP = "\u001E"
        private const val FIELD_SEP = "\u001F"

        internal fun encodeQuickContacts(list: List<QuickContact>): String =
            list.joinToString(RECORD_SEP) { contact ->
                listOf(
                    sanitize(contact.lookupKey),
                    sanitize(contact.displayName),
                    sanitize(contact.phoneNumber),
                ).joinToString(FIELD_SEP)
            }

        internal fun decodeQuickContacts(raw: String?): List<QuickContact> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split(RECORD_SEP).mapNotNull { record ->
                val parts = record.split(FIELD_SEP)
                if (parts.size < 3) return@mapNotNull null
                val key = parts[0]
                val name = parts[1]
                val phone = parts[2]
                if (key.isBlank() || phone.isBlank()) return@mapNotNull null
                QuickContact(
                    lookupKey = key,
                    displayName = name.ifBlank { phone },
                    phoneNumber = phone,
                )
            }
        }

        private fun sanitize(value: String): String =
            value.replace(RECORD_SEP, " ").replace(FIELD_SEP, " ").trim()
    }
}
