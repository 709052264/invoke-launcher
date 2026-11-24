package org.invokevirtual.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.widgetBoardDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "invoke_widget_board",
)

/**
 * Lightweight DataStore persistence for the optional Widget Board's ordered `appWidgetId`s.
 */
class WidgetBoardStore(private val context: Context) {
    private object Keys {
        val WIDGET_IDS = stringPreferencesKey("pinned_widget_ids")
    }

    val widgetIds: Flow<List<Int>> = context.widgetBoardDataStore.data.map { prefs ->
        prefs[Keys.WIDGET_IDS]
            .orEmpty()
            .split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .distinct()
    }

    suspend fun addWidgetId(appWidgetId: Int) {
        context.widgetBoardDataStore.edit { prefs ->
            val current = prefs[Keys.WIDGET_IDS]
                .orEmpty()
                .split(',')
                .mapNotNull { it.trim().toIntOrNull() }
                .toMutableList()
            if (appWidgetId !in current) {
                current += appWidgetId
                prefs[Keys.WIDGET_IDS] = current.joinToString(",")
            }
        }
    }

    suspend fun removeWidgetId(appWidgetId: Int) {
        context.widgetBoardDataStore.edit { prefs ->
            val updated = prefs[Keys.WIDGET_IDS]
                .orEmpty()
                .split(',')
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it != appWidgetId }
            if (updated.isEmpty()) {
                prefs.remove(Keys.WIDGET_IDS)
            } else {
                prefs[Keys.WIDGET_IDS] = updated.joinToString(",")
            }
        }
    }
}
