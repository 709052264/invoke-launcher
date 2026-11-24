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
import org.invokevirtual.launcher.system.AppKey

private val Context.bentoDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "invoke_bento_layout",
)

enum class BentoSlotId(val keyName: String, val label: String) {
    FOCUS("focus", "Focus Capsule"),
    TILE_1("tile_1", "Tile 1"),
    TILE_2("tile_2", "Tile 2"),
    TILE_3("tile_3", "Tile 3"),
    TILE_4("tile_4", "Tile 4"),
    ;

    companion object {
        val tileSlots: List<BentoSlotId> = listOf(TILE_1, TILE_2, TILE_3, TILE_4)
    }
}

@Immutable
data class BentoLayoutConfig(
    val slots: Map<BentoSlotId, AppKey> = emptyMap(),
) {
    fun slotFor(appKey: AppKey): BentoSlotId? =
        slots.entries.firstOrNull { it.value == appKey }?.key
}

/**
 * Simple DataStore-backed persistence for the Home Canvas's sculptural Bento slots (Focus Capsule
 * + 4 Tactile Tiles). No database or serialization library overhead needed.
 */
class BentoLayoutStore(private val context: Context) {

    private fun prefKey(slot: BentoSlotId) = stringPreferencesKey("slot_${slot.keyName}")

    val layout: Flow<BentoLayoutConfig> = context.bentoDataStore.data.map { prefs ->
        val mapped = buildMap {
            for (slot in BentoSlotId.entries) {
                val raw = prefs[prefKey(slot)] ?: continue
                parseAppKey(raw)?.let { put(slot, it) }
            }
        }
        BentoLayoutConfig(slots = mapped)
    }

    /**
     * Pins [appKey] into [slot]. If the same app was already assigned to another Bento slot, it
     * moves cleanly instead of duplicating across the stage.
     */
    suspend fun pinToSlot(slot: BentoSlotId, appKey: AppKey) {
        val encoded = encodeAppKey(appKey)
        context.bentoDataStore.edit { prefs ->
            for (other in BentoSlotId.entries) {
                if (other != slot && prefs[prefKey(other)] == encoded) {
                    prefs.remove(prefKey(other))
                }
            }
            prefs[prefKey(slot)] = encoded
        }
    }

    suspend fun unpinSlot(slot: BentoSlotId) {
        context.bentoDataStore.edit { prefs ->
            prefs.remove(prefKey(slot))
        }
    }

    suspend fun unpinApp(appKey: AppKey) {
        val encoded = encodeAppKey(appKey)
        context.bentoDataStore.edit { prefs ->
            for (slot in BentoSlotId.entries) {
                if (prefs[prefKey(slot)] == encoded) {
                    prefs.remove(prefKey(slot))
                }
            }
        }
    }

    suspend fun resetDefaults() {
        context.bentoDataStore.edit { it.clear() }
    }

    private fun encodeAppKey(key: AppKey): String =
        "${key.packageName}|${key.className}|${key.userSerial}"

    private fun parseAppKey(raw: String): AppKey? {
        val parts = raw.split('|')
        if (parts.size != 3) return null
        val serial = parts[2].toLongOrNull() ?: return null
        return AppKey(
            packageName = parts[0],
            className = parts[1],
            userSerial = serial,
        )
    }
}
