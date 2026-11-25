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
import java.util.UUID

private val Context.homeCanvasDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "invoke_home_canvas",
)

enum class TileSpan(val columns: Int, val label: String) {
    COMPACT(1, "1×1"),
    WIDE(2, "2×1"),
    TALL(1, "1×2"),
    HERO(2, "2×2"),
    WIDE_3X1(3, "3×1"),
    BANNER(4, "4×1"),
    FEATURED(4, "4×2"),
    ;

    fun next(): TileSpan = entries[(ordinal + 1) % entries.size]
}

internal fun defaultHeightDpForSpan(span: TileSpan): Int = when (span) {
    TileSpan.COMPACT -> 76
    TileSpan.WIDE, TileSpan.WIDE_3X1, TileSpan.BANNER -> 82
    TileSpan.TALL, TileSpan.HERO, TileSpan.FEATURED -> 172
}

enum class TileShapePreset(val label: String) {
    SQUIRCLE("Squircle"),
    CIRCLE("Circle"),
    PILL("Pill"),
    LEAF("Leaf"),
    LEAF_ALT("Flip Leaf"),
    ARCH("Arch"),
    TEARDROP("Drop"),
    GEM("Cut"),
    HEXAGON("Hex"),
    COOKIE("Cookie"),
    CLOVER("Clover"),
    SOFT_BURST("Burst"),
    RECTANGLE("Crisp"),
    ;

    fun next(): TileShapePreset = entries[(ordinal + 1) % entries.size]
}

enum class TileTone(val label: String) {
    SURFACE("Surface"),
    PRIMARY("Primary"),
    SECONDARY("Secondary"),
    TERTIARY("Tertiary"),
    ;

    fun next(): TileTone = entries[(ordinal + 1) % entries.size]
}

enum class WidgetHeightTier(
    val columns: Int,
    val heightDp: Int,
    val label: String,
    val isSquare: Boolean = false,
) {
    WIDE_2X1(columns = 2, heightDp = 104, label = "2×1"),
    SQUARE_2X2(columns = 2, heightDp = 176, label = "2×2", isSquare = true),
    COMPACT(columns = 4, heightDp = 116, label = "4×1"),
    MEDIUM(columns = 4, heightDp = 196, label = "4×2"),
    TALL(columns = 4, heightDp = 296, label = "4×3"),
    ;

    fun next(): WidgetHeightTier = entries[(ordinal + 1) % entries.size]
}

enum class FolderLayoutMode(val label: String) {
    BENTO("Bento"),
    GRID("Grid"),
}

@Immutable
sealed interface FolderItem {
    val id: String

    @Immutable
    data class AppItem(
        override val id: String,
        val appKey: AppKey,
        val span: TileSpan = TileSpan.COMPACT,
        val shape: TileShapePreset = TileShapePreset.SQUIRCLE,
        val tone: TileTone = TileTone.SURFACE,
        val customBgArgb: Int? = null,
        val customColumns: Int? = null,
        val customHeightDp: Int? = null,
    ) : FolderItem {
        fun effectiveColumns(gridColumns: Int): Int =
            (customColumns ?: span.columns).coerceIn(1, gridColumns.coerceIn(1, 4))

        val effectiveHeightDp: Int
            get() = (customHeightDp ?: defaultHeightDpForSpan(span)).coerceIn(64, 280)

        val isSquareSize: Boolean
            get() = customColumns == null && customHeightDp == null && span == TileSpan.HERO

        val hasCustomSize: Boolean
            get() = customColumns != null || customHeightDp != null

        fun sizeLabel(gridColumns: Int): String = if (hasCustomSize) {
            "${effectiveColumns(gridColumns)}×${effectiveHeightDp}dp"
        } else {
            span.label
        }
    }

    @Immutable
    data class SpacerItem(
        override val id: String,
        val span: TileSpan = TileSpan.BANNER,
        val customColumns: Int? = null,
        val customHeightDp: Int? = null,
    ) : FolderItem {
        fun effectiveColumns(gridColumns: Int): Int =
            (customColumns ?: span.columns).coerceIn(1, gridColumns.coerceIn(1, 4))

        val effectiveHeightDp: Int
            get() = (customHeightDp ?: defaultHeightDpForSpan(span)).coerceIn(24, 320)

        val isSquareSize: Boolean
            get() = customColumns == null && customHeightDp == null && span == TileSpan.HERO

        val hasCustomSize: Boolean
            get() = customColumns != null || customHeightDp != null

        fun sizeLabel(gridColumns: Int): String = if (hasCustomSize) {
            "${effectiveColumns(gridColumns)}×${effectiveHeightDp}dp"
        } else {
            span.label
        }
    }
}

@Immutable
sealed interface CanvasBlock {
    val id: String
    val shape: TileShapePreset

    @Immutable
    data class AppTile(
        override val id: String,
        val appKey: AppKey,
        val span: TileSpan = TileSpan.COMPACT,
        override val shape: TileShapePreset = TileShapePreset.SQUIRCLE,
        val tone: TileTone = TileTone.SURFACE,
        val customBgArgb: Int? = null,
        val customColumns: Int? = null,
        val customHeightDp: Int? = null,
    ) : CanvasBlock {
        val effectiveColumns: Int
            get() = (customColumns ?: span.columns).coerceIn(1, 4)

        val effectiveHeightDp: Int
            get() = (customHeightDp ?: defaultHeightDpForSpan(span)).coerceIn(64, 280)

        val isSquareSize: Boolean
            get() = customColumns == null && customHeightDp == null && span == TileSpan.HERO

        val hasCustomSize: Boolean
            get() = customColumns != null || customHeightDp != null

        val sizeLabel: String
            get() = if (hasCustomSize) {
                "${effectiveColumns}×${effectiveHeightDp}dp"
            } else {
                span.label
            }
    }

    @Immutable
    data class ShortcutTile(
        override val id: String,
        val packageName: String,
        val shortcutId: String,
        val userSerial: Long,
        val label: String,
        val span: TileSpan = TileSpan.COMPACT,
        override val shape: TileShapePreset = TileShapePreset.LEAF,
        val tone: TileTone = TileTone.SECONDARY,
        val customBgArgb: Int? = null,
        val customColumns: Int? = null,
        val customHeightDp: Int? = null,
    ) : CanvasBlock {
        val effectiveColumns: Int
            get() = (customColumns ?: span.columns).coerceIn(1, 4)

        val effectiveHeightDp: Int
            get() = (customHeightDp ?: defaultHeightDpForSpan(span)).coerceIn(64, 280)

        val isSquareSize: Boolean
            get() = customColumns == null && customHeightDp == null && span == TileSpan.HERO

        val hasCustomSize: Boolean
            get() = customColumns != null || customHeightDp != null

        val sizeLabel: String
            get() = if (hasCustomSize) {
                "${effectiveColumns}×${effectiveHeightDp}dp"
            } else {
                span.label
            }
    }

    @Immutable
    data class ContactTile(
        override val id: String,
        val lookupKey: String,
        val displayName: String,
        val phoneNumber: String,
        val span: TileSpan = TileSpan.COMPACT,
        override val shape: TileShapePreset = TileShapePreset.CIRCLE,
        val tone: TileTone = TileTone.TERTIARY,
        val customBgArgb: Int? = null,
        val customColumns: Int? = null,
        val customHeightDp: Int? = null,
    ) : CanvasBlock {
        val effectiveColumns: Int
            get() = (customColumns ?: span.columns).coerceIn(1, 4)

        val effectiveHeightDp: Int
            get() = (customHeightDp ?: defaultHeightDpForSpan(span)).coerceIn(64, 280)

        val isSquareSize: Boolean
            get() = customColumns == null && customHeightDp == null && span == TileSpan.HERO

        val hasCustomSize: Boolean
            get() = customColumns != null || customHeightDp != null

        val sizeLabel: String
            get() = if (hasCustomSize) {
                "${effectiveColumns}×${effectiveHeightDp}dp"
            } else {
                span.label
            }

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

    @Immutable
    data class WidgetBlock(
        override val id: String,
        val appWidgetId: Int,
        val heightTier: WidgetHeightTier = WidgetHeightTier.MEDIUM,
        override val shape: TileShapePreset = TileShapePreset.SQUIRCLE,
        val customColumns: Int? = null,
        val customHeightDp: Int? = null,
    ) : CanvasBlock {
        val effectiveColumns: Int
            get() = (customColumns ?: heightTier.columns).coerceIn(1, 4)

        val effectiveHeightDp: Int
            get() = (customHeightDp ?: heightTier.heightDp).coerceIn(80, 420)

        val isSquareSize: Boolean
            get() = customColumns == null && customHeightDp == null && heightTier.isSquare

        val hasCustomSize: Boolean
            get() = customColumns != null || customHeightDp != null

        val sizeLabel: String
            get() = if (hasCustomSize) {
                "${effectiveColumns}×${effectiveHeightDp}dp"
            } else {
                heightTier.label
            }
    }

    @Immutable
    data class SpacerBlock(
        override val id: String,
        val span: TileSpan = TileSpan.BANNER,
        override val shape: TileShapePreset = TileShapePreset.SQUIRCLE,
        val customColumns: Int? = null,
        val customHeightDp: Int? = null,
    ) : CanvasBlock {
        val effectiveColumns: Int
            get() = (customColumns ?: span.columns).coerceIn(1, 4)

        val effectiveHeightDp: Int
            get() = (customHeightDp ?: defaultHeightDpForSpan(span)).coerceIn(24, 360)

        val isSquareSize: Boolean
            get() = customColumns == null && customHeightDp == null && span == TileSpan.HERO

        val hasCustomSize: Boolean
            get() = customColumns != null || customHeightDp != null

        val sizeLabel: String
            get() = if (hasCustomSize) {
                "${effectiveColumns}×${effectiveHeightDp}dp"
            } else {
                span.label
            }
    }

    @Immutable
    data class FolderTile(
        override val id: String,
        val title: String,
        val apps: List<AppKey> = emptyList(),
        val span: TileSpan = TileSpan.COMPACT,
        override val shape: TileShapePreset = TileShapePreset.SQUIRCLE,
        val tone: TileTone = TileTone.SURFACE,
        val customBgArgb: Int? = null,
        val customColumns: Int? = null,
        val customHeightDp: Int? = null,
        val layoutMode: FolderLayoutMode = FolderLayoutMode.BENTO,
        val gridColumns: Int = 4,
        val items: List<FolderItem> = emptyList(),
    ) : CanvasBlock {
        val effectiveGridColumns: Int
            get() = gridColumns.coerceIn(2, 4)

        val effectiveItems: List<FolderItem>
            get() = if (items.isNotEmpty()) {
                items
            } else {
                apps.mapIndexed { idx, appKey ->
                    FolderItem.AppItem(
                        id = "fapp_${appKey.packageName}_${appKey.className}_${appKey.userSerial}_$idx",
                        appKey = appKey,
                        shape = shape,
                    )
                }
            }

        fun withUpdatedItems(updatedItems: List<FolderItem>): FolderTile = copy(
            items = updatedItems,
            apps = updatedItems.filterIsInstance<FolderItem.AppItem>().map { it.appKey },
        )

        val effectiveColumns: Int
            get() = (customColumns ?: span.columns).coerceIn(1, 4)

        val effectiveHeightDp: Int
            get() = (customHeightDp ?: defaultHeightDpForSpan(span)).coerceIn(64, 280)

        val isSquareSize: Boolean
            get() = customColumns == null && customHeightDp == null && span == TileSpan.HERO

        val hasCustomSize: Boolean
            get() = customColumns != null || customHeightDp != null

        val sizeLabel: String
            get() = if (hasCustomSize) {
                "${effectiveColumns}×${effectiveHeightDp}dp"
            } else {
                span.label
            }
    }
}

@Immutable
data class HomeCanvasState(
    val blocks: List<CanvasBlock> = emptyList(),
    val isCustomized: Boolean = false,
) {
    val folders: List<CanvasBlock.FolderTile>
        get() = blocks.filterIsInstance<CanvasBlock.FolderTile>()

    fun containsApp(appKey: AppKey): Boolean =
        blocks.any {
            (it is CanvasBlock.AppTile && it.appKey == appKey) ||
                    (it is CanvasBlock.FolderTile && appKey in it.apps)
        }

    fun blockForApp(appKey: AppKey): CanvasBlock.AppTile? =
        blocks.firstOrNull { it is CanvasBlock.AppTile && it.appKey == appKey } as? CanvasBlock.AppTile

    fun containsShortcut(packageName: String, shortcutId: String, userSerial: Long): Boolean =
        blocks.any {
            it is CanvasBlock.ShortcutTile &&
                it.packageName == packageName &&
                it.shortcutId == shortcutId &&
                it.userSerial == userSerial
        }

    fun containsContact(lookupKey: String): Boolean =
        blocks.any { it is CanvasBlock.ContactTile && it.lookupKey == lookupKey }

    fun blockForContact(lookupKey: String): CanvasBlock.ContactTile? =
        blocks.firstOrNull { it is CanvasBlock.ContactTile && it.lookupKey == lookupKey } as? CanvasBlock.ContactTile
}

/**
 * DataStore-backed persistence for Invoke's Home Canvas: resizable, reshapable, color-customizable
 * app tiles, pinned shortcuts, and edge-to-edge native Android widgets.
 */
class HomeCanvasStore(private val context: Context) {

    private object Keys {
        val BLOCKS_RAW = stringPreferencesKey("canvas_blocks_v1")
    }

    val canvasState: Flow<HomeCanvasState> = context.homeCanvasDataStore.data.map { prefs ->
        val raw = prefs[Keys.BLOCKS_RAW]
        if (raw == null) {
            HomeCanvasState(blocks = emptyList(), isCustomized = false)
        } else {
            HomeCanvasState(blocks = decodeBlocks(raw), isCustomized = true)
        }
    }

    /** Mutates the block list, seeding from [fallbackDefaults] on the user's first edit. */
    private suspend fun updateBlocks(
        fallbackDefaults: List<CanvasBlock>,
        transform: (List<CanvasBlock>) -> List<CanvasBlock>,
    ) {
        context.homeCanvasDataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKS_RAW]?.let(::decodeBlocks) ?: fallbackDefaults
            val updated = transform(current).take(MAX_BLOCKS)
            prefs[Keys.BLOCKS_RAW] = encodeBlocks(updated)
        }
    }

    suspend fun addAppTile(
        appKey: AppKey,
        fallbackDefaults: List<CanvasBlock>,
        span: TileSpan = TileSpan.COMPACT,
        shape: TileShapePreset = TileShapePreset.SQUIRCLE,
        tone: TileTone = TileTone.SURFACE,
        customBgArgb: Int? = null,
        customColumns: Int? = null,
        customHeightDp: Int? = null,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            if (list.any { it is CanvasBlock.AppTile && it.appKey == appKey }) {
                list
            } else {
                list + CanvasBlock.AppTile(
                    id = "app_${UUID.randomUUID().toString().take(8)}",
                    appKey = appKey,
                    span = span,
                    shape = shape,
                    tone = tone,
                    customBgArgb = customBgArgb,
                    customColumns = customColumns?.coerceIn(1, 4),
                    customHeightDp = customHeightDp?.coerceIn(64, 280),
                )
            }
        }
    }

    suspend fun addShortcutTile(
        packageName: String,
        shortcutId: String,
        userSerial: Long,
        label: String,
        fallbackDefaults: List<CanvasBlock>,
        span: TileSpan = TileSpan.COMPACT,
        shape: TileShapePreset = TileShapePreset.LEAF,
        tone: TileTone = TileTone.SECONDARY,
        customBgArgb: Int? = null,
        customColumns: Int? = null,
        customHeightDp: Int? = null,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            val alreadyPinned = list.any {
                it is CanvasBlock.ShortcutTile &&
                    it.packageName == packageName &&
                    it.shortcutId == shortcutId &&
                    it.userSerial == userSerial
            }
            if (alreadyPinned) {
                list
            } else {
                list + CanvasBlock.ShortcutTile(
                    id = "sc_${UUID.randomUUID().toString().take(8)}",
                    packageName = packageName,
                    shortcutId = shortcutId,
                    userSerial = userSerial,
                    label = label.ifBlank { shortcutId },
                    span = span,
                    shape = shape,
                    tone = tone,
                    customBgArgb = customBgArgb,
                    customColumns = customColumns?.coerceIn(1, 4),
                    customHeightDp = customHeightDp?.coerceIn(64, 280),
                )
            }
        }
    }

    suspend fun addWidgetBlock(
        appWidgetId: Int,
        fallbackDefaults: List<CanvasBlock>,
        heightTier: WidgetHeightTier = WidgetHeightTier.MEDIUM,
        shape: TileShapePreset = TileShapePreset.SQUIRCLE,
        customColumns: Int? = null,
        customHeightDp: Int? = null,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            if (list.any { it is CanvasBlock.WidgetBlock && it.appWidgetId == appWidgetId }) {
                list
            } else {
                val widgetBlock = CanvasBlock.WidgetBlock(
                    id = "widget_$appWidgetId",
                    appWidgetId = appWidgetId,
                    heightTier = heightTier,
                    shape = shape,
                    customColumns = customColumns?.coerceIn(1, 4),
                    customHeightDp = customHeightDp?.coerceIn(80, 420),
                )
                listOf(widgetBlock) + list
            }
        }
    }

    suspend fun removeBlock(
        blockId: String,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.filterNot { it.id == blockId }
        }
    }

    suspend fun removeApp(
        appKey: AppKey,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.filterNot { it is CanvasBlock.AppTile && it.appKey == appKey }
        }
    }

    suspend fun removeShortcut(
        packageName: String,
        shortcutId: String,
        userSerial: Long,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.filterNot {
                it is CanvasBlock.ShortcutTile &&
                    it.packageName == packageName &&
                    it.shortcutId == shortcutId &&
                    it.userSerial == userSerial
            }
        }
    }

    suspend fun addContactTile(
        contact: QuickContact,
        fallbackDefaults: List<CanvasBlock>,
        span: TileSpan = TileSpan.COMPACT,
        shape: TileShapePreset = TileShapePreset.CIRCLE,
        tone: TileTone = TileTone.TERTIARY,
        customBgArgb: Int? = null,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            if (list.any { it is CanvasBlock.ContactTile && it.lookupKey == contact.lookupKey }) {
                list
            } else {
                list + CanvasBlock.ContactTile(
                    id = "ct_${UUID.randomUUID().toString().take(8)}",
                    lookupKey = contact.lookupKey,
                    displayName = contact.displayName,
                    phoneNumber = contact.phoneNumber,
                    span = span,
                    shape = shape,
                    tone = tone,
                    customBgArgb = customBgArgb,
                )
            }
        }
    }

    suspend fun removeContactTile(
        lookupKey: String,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.filterNot { it is CanvasBlock.ContactTile && it.lookupKey == lookupKey }
        }
    }

    suspend fun moveBlock(
        blockId: String,
        delta: Int,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            val index = list.indexOfFirst { it.id == blockId }
            if (index == -1) return@updateBlocks list
            val target = (index + delta).coerceIn(0, list.lastIndex)
            if (target == index) return@updateBlocks list
            val mutable = list.toMutableList()
            val item = mutable.removeAt(index)
            mutable.add(target, item)
            mutable
        }
    }

    suspend fun addSpacerBlock(
        fallbackDefaults: List<CanvasBlock>,
        span: TileSpan = TileSpan.BANNER,
        customColumns: Int? = null,
        customHeightDp: Int? = null,
        explicitId: String = "spc_${UUID.randomUUID().toString().take(8)}",
    ) {
        updateBlocks(fallbackDefaults) { list ->
            val spacer = CanvasBlock.SpacerBlock(
                id = explicitId,
                span = span,
                customColumns = customColumns?.coerceIn(1, 4),
                customHeightDp = customHeightDp?.coerceIn(24, 360),
            )
            listOf(spacer) + list
        }
    }

    suspend fun addFolderTile(
        title: String,
        apps: List<AppKey> = emptyList(),
        fallbackDefaults: List<CanvasBlock>,
        span: TileSpan = TileSpan.COMPACT,
        shape: TileShapePreset = TileShapePreset.SQUIRCLE,
        tone: TileTone = TileTone.SURFACE,
        customBgArgb: Int? = null,
        customColumns: Int? = null,
        customHeightDp: Int? = null,
    ) {
        val cleanTitle = title.trim().ifBlank { "Folder" }
        val distinctApps = apps.distinct()
        val initialItems = distinctApps.mapIndexed { idx, key ->
            FolderItem.AppItem(
                id = "fapp_${UUID.randomUUID().toString().take(6)}_$idx",
                appKey = key,
                shape = shape,
            )
        }
        updateBlocks(fallbackDefaults) { list ->
            list + CanvasBlock.FolderTile(
                id = "fld_${UUID.randomUUID().toString().take(8)}",
                title = cleanTitle,
                apps = distinctApps,
                span = span,
                shape = shape,
                tone = tone,
                customBgArgb = customBgArgb,
                customColumns = customColumns?.coerceIn(1, 4),
                customHeightDp = customHeightDp?.coerceIn(64, 280),
                items = initialItems,
            )
        }
    }

    suspend fun updateFolder(
        folderId: String,
        title: String,
        apps: List<AppKey>,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        val cleanTitle = title.trim().ifBlank { "Folder" }
        val distinctApps = apps.distinct()
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val existingByKey = block.effectiveItems
                        .filterIsInstance<FolderItem.AppItem>()
                        .associateBy { it.appKey }
                    val keptAppKeys = distinctApps.toSet()
                    val preservedItems = block.effectiveItems.filter { item ->
                        when (item) {
                            is FolderItem.SpacerItem -> true
                            is FolderItem.AppItem -> item.appKey in keptAppKeys
                        }
                    }
                    val existingKeys = preservedItems
                        .filterIsInstance<FolderItem.AppItem>()
                        .mapTo(mutableSetOf()) { it.appKey }
                    val newlyAdded = distinctApps
                        .filter { it !in existingKeys }
                        .map { key ->
                            existingByKey[key] ?: FolderItem.AppItem(
                                id = "fapp_${UUID.randomUUID().toString().take(8)}",
                                appKey = key,
                                shape = block.shape,
                            )
                        }
                    block.copy(title = cleanTitle).withUpdatedItems(preservedItems + newlyAdded)
                } else {
                    block
                }
            }
        }
    }

    suspend fun renameFolder(
        folderId: String,
        title: String,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        val cleanTitle = title.trim().ifBlank { "Folder" }
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    block.copy(title = cleanTitle)
                } else {
                    block
                }
            }
        }
    }

    suspend fun setFolderApps(
        folderId: String,
        apps: List<AppKey>,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        val distinctApps = apps.distinct()
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val existingByKey = block.effectiveItems
                        .filterIsInstance<FolderItem.AppItem>()
                        .associateBy { it.appKey }
                    val kept = distinctApps.toSet()
                    val preserved = block.effectiveItems.filter {
                        it is FolderItem.SpacerItem || (it is FolderItem.AppItem && it.appKey in kept)
                    }
                    val existingKeys = preserved
                        .filterIsInstance<FolderItem.AppItem>()
                        .mapTo(mutableSetOf()) { it.appKey }
                    val added = distinctApps
                        .filter { it !in existingKeys }
                        .map { key ->
                            existingByKey[key] ?: FolderItem.AppItem(
                                id = "fapp_${UUID.randomUUID().toString().take(8)}",
                                appKey = key,
                                shape = block.shape,
                            )
                        }
                    block.withUpdatedItems(preserved + added)
                } else {
                    block
                }
            }
        }
    }

    suspend fun addAppToFolder(
        folderId: String,
        appKey: AppKey,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    if (appKey in block.apps) {
                        block
                    } else {
                        val newItem = FolderItem.AppItem(
                            id = "fapp_${UUID.randomUUID().toString().take(8)}",
                            appKey = appKey,
                            shape = block.shape,
                        )
                        block.withUpdatedItems(block.effectiveItems + newItem)
                    }
                } else {
                    block
                }
            }
        }
    }

    suspend fun removeAppFromFolder(
        folderId: String,
        appKey: AppKey,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val updated = block.effectiveItems.filterNot {
                        it is FolderItem.AppItem && it.appKey == appKey
                    }
                    block.withUpdatedItems(updated)
                } else {
                    block
                }
            }
        }
    }

    suspend fun moveAppInFolder(
        folderId: String,
        appKey: AppKey,
        delta: Int,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val items = block.effectiveItems
                    val idx = items.indexOfFirst { it is FolderItem.AppItem && it.appKey == appKey }
                    if (idx == -1) return@map block
                    val target = (idx + delta).coerceIn(0, items.lastIndex)
                    if (target == idx) return@map block
                    val mutable = items.toMutableList()
                    val moved = mutable.removeAt(idx)
                    mutable.add(target, moved)
                    block.withUpdatedItems(mutable)
                } else {
                    block
                }
            }
        }
    }

    suspend fun setFolderLayout(
        folderId: String,
        layoutMode: FolderLayoutMode,
        gridColumns: Int,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        val clampedCols = gridColumns.coerceIn(2, 4)
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    block.copy(
                        layoutMode = layoutMode,
                        gridColumns = clampedCols,
                    )
                } else {
                    block
                }
            }
        }
    }

    suspend fun addSpacerToFolder(
        folderId: String,
        span: TileSpan = TileSpan.BANNER,
        customColumns: Int? = null,
        customHeightDp: Int? = null,
        explicitId: String = "fspc_${UUID.randomUUID().toString().take(8)}",
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val spacer = FolderItem.SpacerItem(
                        id = explicitId,
                        span = span,
                        customColumns = customColumns?.coerceIn(1, 4),
                        customHeightDp = customHeightDp?.coerceIn(24, 320),
                    )
                    block.withUpdatedItems(listOf(spacer) + block.effectiveItems)
                } else {
                    block
                }
            }
        }
    }

    suspend fun removeFolderItem(
        folderId: String,
        itemId: String,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    block.withUpdatedItems(block.effectiveItems.filterNot { it.id == itemId })
                } else {
                    block
                }
            }
        }
    }

    suspend fun moveFolderItem(
        folderId: String,
        itemId: String,
        delta: Int,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val items = block.effectiveItems
                    val idx = items.indexOfFirst { it.id == itemId }
                    if (idx == -1) return@map block
                    val target = (idx + delta).coerceIn(0, items.lastIndex)
                    if (target == idx) return@map block
                    val mutable = items.toMutableList()
                    val moved = mutable.removeAt(idx)
                    mutable.add(target, moved)
                    block.withUpdatedItems(mutable)
                } else {
                    block
                }
            }
        }
    }

    suspend fun setFolderItemSpan(
        folderId: String,
        itemId: String,
        span: TileSpan,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val updated = block.effectiveItems.map { item ->
                        if (item.id != itemId) {
                            item
                        } else {
                            when (item) {
                                is FolderItem.AppItem -> item.copy(
                                    span = span,
                                    customColumns = null,
                                    customHeightDp = null,
                                )

                                is FolderItem.SpacerItem -> item.copy(
                                    span = span,
                                    customColumns = null,
                                    customHeightDp = null,
                                )
                            }
                        }
                    }
                    block.withUpdatedItems(updated)
                } else {
                    block
                }
            }
        }
    }

    suspend fun setFolderItemCustomSize(
        folderId: String,
        itemId: String,
        columns: Int,
        heightDp: Int,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        val clampedCols = columns.coerceIn(1, 4)
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val updated = block.effectiveItems.map { item ->
                        if (item.id != itemId) {
                            item
                        } else {
                            when (item) {
                                is FolderItem.AppItem -> item.copy(
                                    customColumns = clampedCols,
                                    customHeightDp = heightDp.coerceIn(64, 280),
                                )

                                is FolderItem.SpacerItem -> item.copy(
                                    customColumns = clampedCols,
                                    customHeightDp = heightDp.coerceIn(24, 320),
                                )
                            }
                        }
                    }
                    block.withUpdatedItems(updated)
                } else {
                    block
                }
            }
        }
    }

    suspend fun setFolderItemShape(
        folderId: String,
        itemId: String,
        shape: TileShapePreset,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val updated = block.effectiveItems.map { item ->
                        if (item.id == itemId && item is FolderItem.AppItem) {
                            item.copy(shape = shape)
                        } else {
                            item
                        }
                    }
                    block.withUpdatedItems(updated)
                } else {
                    block
                }
            }
        }
    }

    suspend fun setFolderItemTone(
        folderId: String,
        itemId: String,
        tone: TileTone,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val updated = block.effectiveItems.map { item ->
                        if (item.id == itemId && item is FolderItem.AppItem) {
                            item.copy(tone = tone, customBgArgb = null)
                        } else {
                            item
                        }
                    }
                    block.withUpdatedItems(updated)
                } else {
                    block
                }
            }
        }
    }

    suspend fun setFolderItemCustomBgColor(
        folderId: String,
        itemId: String,
        argb: Int?,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        val opaqueArgb = argb?.let { it or (0xFF shl 24) }
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == folderId && block is CanvasBlock.FolderTile) {
                    val updated = block.effectiveItems.map { item ->
                        if (item.id == itemId && item is FolderItem.AppItem) {
                            item.copy(customBgArgb = opaqueArgb)
                        } else {
                            item
                        }
                    }
                    block.withUpdatedItems(updated)
                } else {
                    block
                }
            }
        }
    }

    suspend fun cycleBlockSize(
        blockId: String,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id != blockId) {
                    block
                } else {
                    when (block) {
                        is CanvasBlock.AppTile -> block.copy(span = block.span.next())
                        is CanvasBlock.ShortcutTile -> block.copy(span = block.span.next())
                        is CanvasBlock.ContactTile -> block.copy(span = block.span.next())
                        is CanvasBlock.SpacerBlock -> block.copy(span = block.span.next())
                        is CanvasBlock.FolderTile -> block.copy(span = block.span.next())
                        is CanvasBlock.WidgetBlock -> block.copy(heightTier = block.heightTier.next())
                    }
                }
            }
        }
    }

    suspend fun setBlockSpan(
        blockId: String,
        span: TileSpan,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id != blockId) {
                    block
                } else {
                    when (block) {
                        is CanvasBlock.AppTile -> block.copy(
                            span = span,
                            customColumns = null,
                            customHeightDp = null,
                        )
                        is CanvasBlock.ShortcutTile -> block.copy(
                            span = span,
                            customColumns = null,
                            customHeightDp = null,
                        )
                        is CanvasBlock.ContactTile -> block.copy(
                            span = span,
                            customColumns = null,
                            customHeightDp = null,
                        )
                        is CanvasBlock.SpacerBlock -> block.copy(
                            span = span,
                            customColumns = null,
                            customHeightDp = null,
                        )

                        is CanvasBlock.FolderTile -> block.copy(
                            span = span,
                            customColumns = null,
                            customHeightDp = null,
                        )
                        is CanvasBlock.WidgetBlock -> block
                    }
                }
            }
        }
    }

    suspend fun setTileCustomSize(
        blockId: String,
        columns: Int,
        heightDp: Int,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        val clampedCols = columns.coerceIn(1, 4)
        val clampedTileHeight = heightDp.coerceIn(64, 280)
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id != blockId) {
                    block
                } else {
                    when (block) {
                        is CanvasBlock.AppTile -> block.copy(
                            customColumns = clampedCols,
                            customHeightDp = clampedTileHeight,
                        )
                        is CanvasBlock.ShortcutTile -> block.copy(
                            customColumns = clampedCols,
                            customHeightDp = clampedTileHeight,
                        )
                        is CanvasBlock.ContactTile -> block.copy(
                            customColumns = clampedCols,
                            customHeightDp = clampedTileHeight,
                        )

                        is CanvasBlock.SpacerBlock -> block.copy(
                            customColumns = clampedCols,
                            customHeightDp = heightDp.coerceIn(24, 360),
                        )

                        is CanvasBlock.FolderTile -> block.copy(
                            customColumns = clampedCols,
                            customHeightDp = clampedTileHeight,
                        )
                        is CanvasBlock.WidgetBlock -> block
                    }
                }
            }
        }
    }

    suspend fun setWidgetSize(
        blockId: String,
        tier: WidgetHeightTier,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == blockId && block is CanvasBlock.WidgetBlock) {
                    block.copy(
                        heightTier = tier,
                        customColumns = null,
                        customHeightDp = null,
                    )
                } else {
                    block
                }
            }
        }
    }

    suspend fun setWidgetCustomSize(
        blockId: String,
        columns: Int,
        heightDp: Int,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        val clampedCols = columns.coerceIn(1, 4)
        val clampedHeight = heightDp.coerceIn(80, 420)
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id == blockId && block is CanvasBlock.WidgetBlock) {
                    block.copy(
                        customColumns = clampedCols,
                        customHeightDp = clampedHeight,
                    )
                } else {
                    block
                }
            }
        }
    }

    suspend fun cycleBlockShape(
        blockId: String,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id != blockId) {
                    block
                } else {
                    when (block) {
                        is CanvasBlock.AppTile -> block.copy(shape = block.shape.next())
                        is CanvasBlock.ShortcutTile -> block.copy(shape = block.shape.next())
                        is CanvasBlock.ContactTile -> block.copy(shape = block.shape.next())
                        is CanvasBlock.SpacerBlock -> block
                        is CanvasBlock.FolderTile -> block.copy(shape = block.shape.next())
                        is CanvasBlock.WidgetBlock -> block.copy(shape = block.shape.next())
                    }
                }
            }
        }
    }

    suspend fun setBlockShape(
        blockId: String,
        shape: TileShapePreset,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id != blockId) {
                    block
                } else {
                    when (block) {
                        is CanvasBlock.AppTile -> block.copy(shape = shape)
                        is CanvasBlock.ShortcutTile -> block.copy(shape = shape)
                        is CanvasBlock.ContactTile -> block.copy(shape = shape)
                        is CanvasBlock.SpacerBlock -> block
                        is CanvasBlock.FolderTile -> block.copy(shape = shape)
                        is CanvasBlock.WidgetBlock -> block.copy(shape = shape)
                    }
                }
            }
        }
    }

    suspend fun cycleAppTileTone(
        blockId: String,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id != blockId) {
                    block
                } else {
                    when (block) {
                        is CanvasBlock.AppTile -> block.copy(tone = block.tone.next(), customBgArgb = null)
                        is CanvasBlock.ShortcutTile -> block.copy(tone = block.tone.next(), customBgArgb = null)
                        is CanvasBlock.ContactTile -> block.copy(tone = block.tone.next(), customBgArgb = null)
                        is CanvasBlock.SpacerBlock -> block
                        is CanvasBlock.FolderTile -> block.copy(
                            tone = block.tone.next(),
                            customBgArgb = null
                        )
                        is CanvasBlock.WidgetBlock -> block
                    }
                }
            }
        }
    }

    suspend fun setBlockTone(
        blockId: String,
        tone: TileTone,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id != blockId) {
                    block
                } else {
                    when (block) {
                        is CanvasBlock.AppTile -> block.copy(tone = tone, customBgArgb = null)
                        is CanvasBlock.ShortcutTile -> block.copy(tone = tone, customBgArgb = null)
                        is CanvasBlock.ContactTile -> block.copy(tone = tone, customBgArgb = null)
                        is CanvasBlock.SpacerBlock -> block
                        is CanvasBlock.FolderTile -> block.copy(tone = tone, customBgArgb = null)
                        is CanvasBlock.WidgetBlock -> block
                    }
                }
            }
        }
    }

    suspend fun setBlockCustomBgColor(
        blockId: String,
        argb: Int?,
        fallbackDefaults: List<CanvasBlock>,
    ) {
        val opaqueArgb = argb?.let { it or (0xFF shl 24) }
        updateBlocks(fallbackDefaults) { list ->
            list.map { block ->
                if (block.id != blockId) {
                    block
                } else {
                    when (block) {
                        is CanvasBlock.AppTile -> block.copy(customBgArgb = opaqueArgb)
                        is CanvasBlock.ShortcutTile -> block.copy(customBgArgb = opaqueArgb)
                        is CanvasBlock.ContactTile -> block.copy(customBgArgb = opaqueArgb)
                        is CanvasBlock.SpacerBlock -> block
                        is CanvasBlock.FolderTile -> block.copy(customBgArgb = opaqueArgb)
                        is CanvasBlock.WidgetBlock -> block
                    }
                }
            }
        }
    }

    suspend fun resetDefaults() {
        context.homeCanvasDataStore.edit { it.remove(Keys.BLOCKS_RAW) }
    }

    companion object {
        private const val MAX_BLOCKS = 32
        private const val RECORD_SEP = '\u001E'
        private const val FIELD_SEP = '\u001F'
        private const val ITEM_SEP = '\u001D'
        private const val SUBFIELD_SEP = '\u001C'

        internal fun encodeBlocks(blocks: List<CanvasBlock>): String =
            blocks.joinToString(RECORD_SEP.toString()) { block ->
                when (block) {
                    is CanvasBlock.AppTile -> listOf(
                        "A",
                        block.id.clean(),
                        block.appKey.packageName.clean(),
                        block.appKey.className.clean(),
                        block.appKey.userSerial.toString(),
                        block.span.name,
                        block.shape.name,
                        block.tone.name,
                        block.customBgArgb?.toString().orEmpty(),
                        block.customColumns?.toString().orEmpty(),
                        block.customHeightDp?.toString().orEmpty(),
                    ).joinToString(FIELD_SEP.toString())

                    is CanvasBlock.ShortcutTile -> listOf(
                        "S",
                        block.id.clean(),
                        block.packageName.clean(),
                        block.shortcutId.clean(),
                        block.userSerial.toString(),
                        block.label.clean(),
                        block.span.name,
                        block.shape.name,
                        block.tone.name,
                        block.customBgArgb?.toString().orEmpty(),
                        block.customColumns?.toString().orEmpty(),
                        block.customHeightDp?.toString().orEmpty(),
                    ).joinToString(FIELD_SEP.toString())

                    is CanvasBlock.WidgetBlock -> listOf(
                        "W",
                        block.id.clean(),
                        block.appWidgetId.toString(),
                        block.heightTier.name,
                        block.shape.name,
                        block.customColumns?.toString().orEmpty(),
                        block.customHeightDp?.toString().orEmpty(),
                    ).joinToString(FIELD_SEP.toString())

                    is CanvasBlock.ContactTile -> listOf(
                        "C",
                        block.id.clean(),
                        block.lookupKey.clean(),
                        block.displayName.clean(),
                        block.phoneNumber.clean(),
                        block.span.name,
                        block.shape.name,
                        block.tone.name,
                        block.customBgArgb?.toString().orEmpty(),
                        block.customColumns?.toString().orEmpty(),
                        block.customHeightDp?.toString().orEmpty(),
                    ).joinToString(FIELD_SEP.toString())

                    is CanvasBlock.SpacerBlock -> listOf(
                        "E",
                        block.id.clean(),
                        block.span.name,
                        block.customColumns?.toString().orEmpty(),
                        block.customHeightDp?.toString().orEmpty(),
                    ).joinToString(FIELD_SEP.toString())

                    is CanvasBlock.FolderTile -> {
                        val encodedApps = block.apps.joinToString(ITEM_SEP.toString()) { appKey ->
                            listOf(
                                appKey.packageName.clean(),
                                appKey.className.clean(),
                                appKey.userSerial.toString(),
                            ).joinToString(SUBFIELD_SEP.toString())
                        }
                        val encodedItems = if (block.items.isEmpty()) {
                            ""
                        } else {
                            block.items.joinToString(ITEM_SEP.toString()) { item ->
                                when (item) {
                                    is FolderItem.AppItem -> listOf(
                                        "A",
                                        item.id.clean(),
                                        item.appKey.packageName.clean(),
                                        item.appKey.className.clean(),
                                        item.appKey.userSerial.toString(),
                                        item.span.name,
                                        item.shape.name,
                                        item.tone.name,
                                        item.customBgArgb?.toString().orEmpty(),
                                        item.customColumns?.toString().orEmpty(),
                                        item.customHeightDp?.toString().orEmpty(),
                                    ).joinToString(SUBFIELD_SEP.toString())

                                    is FolderItem.SpacerItem -> listOf(
                                        "E",
                                        item.id.clean(),
                                        item.span.name,
                                        item.customColumns?.toString().orEmpty(),
                                        item.customHeightDp?.toString().orEmpty(),
                                    ).joinToString(SUBFIELD_SEP.toString())
                                }
                            }
                        }
                        val base = listOf(
                            "F",
                            block.id.clean(),
                            block.title.clean(),
                            encodedApps,
                            block.span.name,
                            block.shape.name,
                            block.tone.name,
                            block.customBgArgb?.toString().orEmpty(),
                            block.customColumns?.toString().orEmpty(),
                            block.customHeightDp?.toString().orEmpty(),
                        )
                        val extended = if (
                            block.layoutMode != FolderLayoutMode.BENTO ||
                            block.gridColumns != 4 ||
                            encodedItems.isNotEmpty()
                        ) {
                            listOf(
                                block.layoutMode.name,
                                block.effectiveGridColumns.toString(),
                                encodedItems,
                            )
                        } else {
                            emptyList()
                        }
                        (base + extended).joinToString(FIELD_SEP.toString())
                    }
                }
            }

        internal fun decodeBlocks(raw: String): List<CanvasBlock> {
            if (raw.isEmpty()) return emptyList()
            return raw.split(RECORD_SEP).mapNotNull { record ->
                val parts = record.split(FIELD_SEP)
                when (parts.firstOrNull()) {
                    "A" -> {
                        if (parts.size < 8) return@mapNotNull null
                        val serial = parts[4].toLongOrNull() ?: return@mapNotNull null
                        val span = runCatching { TileSpan.valueOf(parts[5]) }.getOrDefault(TileSpan.COMPACT)
                        val shape = runCatching { TileShapePreset.valueOf(parts[6]) }.getOrDefault(TileShapePreset.SQUIRCLE)
                        val tone = runCatching { TileTone.valueOf(parts[7]) }.getOrDefault(TileTone.SURFACE)
                        val customBgArgb = parts.getOrNull(8)?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                        val customColumns = parts.getOrNull(9)?.takeIf { it.isNotEmpty() }?.toIntOrNull()?.coerceIn(1, 4)
                        val customHeightDp = parts.getOrNull(10)?.takeIf { it.isNotEmpty() }?.toIntOrNull()?.coerceIn(64, 280)
                        CanvasBlock.AppTile(
                            id = parts[1],
                            appKey = AppKey(parts[2], parts[3], serial),
                            span = span,
                            shape = shape,
                            tone = tone,
                            customBgArgb = customBgArgb,
                            customColumns = customColumns,
                            customHeightDp = customHeightDp,
                        )
                    }

                    "S" -> {
                        if (parts.size < 9) return@mapNotNull null
                        val serial = parts[4].toLongOrNull() ?: return@mapNotNull null
                        val span = runCatching { TileSpan.valueOf(parts[6]) }.getOrDefault(TileSpan.COMPACT)
                        val shape = runCatching { TileShapePreset.valueOf(parts[7]) }.getOrDefault(TileShapePreset.LEAF)
                        val tone = runCatching { TileTone.valueOf(parts[8]) }.getOrDefault(TileTone.SECONDARY)
                        val customBgArgb = parts.getOrNull(9)?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                        val customColumns = parts.getOrNull(10)?.takeIf { it.isNotEmpty() }?.toIntOrNull()?.coerceIn(1, 4)
                        val customHeightDp = parts.getOrNull(11)?.takeIf { it.isNotEmpty() }?.toIntOrNull()?.coerceIn(64, 280)
                        CanvasBlock.ShortcutTile(
                            id = parts[1],
                            packageName = parts[2],
                            shortcutId = parts[3],
                            userSerial = serial,
                            label = parts[5],
                            span = span,
                            shape = shape,
                            tone = tone,
                            customBgArgb = customBgArgb,
                            customColumns = customColumns,
                            customHeightDp = customHeightDp,
                        )
                    }

                    "C" -> {
                        if (parts.size < 8) return@mapNotNull null
                        val span = runCatching { TileSpan.valueOf(parts[5]) }.getOrDefault(TileSpan.COMPACT)
                        val shape = runCatching { TileShapePreset.valueOf(parts[6]) }.getOrDefault(TileShapePreset.CIRCLE)
                        val tone = runCatching { TileTone.valueOf(parts[7]) }.getOrDefault(TileTone.TERTIARY)
                        val customBgArgb = parts.getOrNull(8)?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                        val customColumns = parts.getOrNull(9)?.takeIf { it.isNotEmpty() }?.toIntOrNull()?.coerceIn(1, 4)
                        val customHeightDp = parts.getOrNull(10)?.takeIf { it.isNotEmpty() }?.toIntOrNull()?.coerceIn(64, 280)
                        CanvasBlock.ContactTile(
                            id = parts[1],
                            lookupKey = parts[2],
                            displayName = parts[3],
                            phoneNumber = parts[4],
                            span = span,
                            shape = shape,
                            tone = tone,
                            customBgArgb = customBgArgb,
                            customColumns = customColumns,
                            customHeightDp = customHeightDp,
                        )
                    }

                    "W" -> {
                        if (parts.size < 4) return@mapNotNull null
                        val widgetId = parts[2].toIntOrNull() ?: return@mapNotNull null
                        val tier = runCatching { WidgetHeightTier.valueOf(parts[3]) }.getOrDefault(WidgetHeightTier.MEDIUM)
                        val shape = parts.getOrNull(4)
                            ?.let { runCatching { TileShapePreset.valueOf(it) }.getOrNull() }
                            ?: TileShapePreset.SQUIRCLE
                        val customColumns = parts.getOrNull(5)?.takeIf { it.isNotEmpty() }?.toIntOrNull()?.coerceIn(1, 4)
                        val customHeightDp = parts.getOrNull(6)?.takeIf { it.isNotEmpty() }?.toIntOrNull()?.coerceIn(80, 420)
                        CanvasBlock.WidgetBlock(
                            id = parts[1],
                            appWidgetId = widgetId,
                            heightTier = tier,
                            shape = shape,
                            customColumns = customColumns,
                            customHeightDp = customHeightDp,
                        )
                    }

                    "E" -> {
                        if (parts.size < 3) return@mapNotNull null
                        val span =
                            runCatching { TileSpan.valueOf(parts[2]) }.getOrDefault(TileSpan.BANNER)
                        val customColumns =
                            parts.getOrNull(3)?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                                ?.coerceIn(1, 4)
                        val customHeightDp =
                            parts.getOrNull(4)?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                                ?.coerceIn(24, 360)
                        CanvasBlock.SpacerBlock(
                            id = parts[1],
                            span = span,
                            customColumns = customColumns,
                            customHeightDp = customHeightDp,
                        )
                    }

                    "F" -> {
                        if (parts.size < 7) return@mapNotNull null
                        val apps = if (parts[3].isEmpty()) {
                            emptyList()
                        } else {
                            parts[3].split(ITEM_SEP).mapNotNull { entry ->
                                val sub = entry.split(SUBFIELD_SEP)
                                if (sub.size < 3) return@mapNotNull null
                                val serial = sub[2].toLongOrNull() ?: return@mapNotNull null
                                AppKey(sub[0], sub[1], serial)
                            }
                        }
                        val span =
                            runCatching { TileSpan.valueOf(parts[4]) }.getOrDefault(TileSpan.COMPACT)
                        val shape = runCatching { TileShapePreset.valueOf(parts[5]) }.getOrDefault(
                            TileShapePreset.SQUIRCLE
                        )
                        val tone =
                            runCatching { TileTone.valueOf(parts[6]) }.getOrDefault(TileTone.SURFACE)
                        val customBgArgb =
                            parts.getOrNull(7)?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                        val customColumns =
                            parts.getOrNull(8)?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                                ?.coerceIn(1, 4)
                        val customHeightDp =
                            parts.getOrNull(9)?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                                ?.coerceIn(64, 280)
                        val layoutMode = parts.getOrNull(10)
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { runCatching { FolderLayoutMode.valueOf(it) }.getOrNull() }
                            ?: FolderLayoutMode.BENTO
                        val gridColumns = parts.getOrNull(11)?.toIntOrNull()?.coerceIn(2, 4) ?: 4
                        val items =
                            parts.getOrNull(12)?.takeIf { it.isNotEmpty() }?.let { rawItems ->
                                rawItems.split(ITEM_SEP).mapNotNull { entry ->
                                    val sub = entry.split(SUBFIELD_SEP)
                                    when (sub.firstOrNull()) {
                                        "A" -> {
                                            if (sub.size < 8) return@mapNotNull null
                                            val serial =
                                                sub[4].toLongOrNull() ?: return@mapNotNull null
                                            FolderItem.AppItem(
                                                id = sub[1],
                                                appKey = AppKey(sub[2], sub[3], serial),
                                                span = runCatching { TileSpan.valueOf(sub[5]) }.getOrDefault(
                                                    TileSpan.COMPACT
                                                ),
                                                shape = runCatching { TileShapePreset.valueOf(sub[6]) }.getOrDefault(
                                                    shape
                                                ),
                                                tone = runCatching { TileTone.valueOf(sub[7]) }.getOrDefault(
                                                    TileTone.SURFACE
                                                ),
                                                customBgArgb = sub.getOrNull(8)
                                                    ?.takeIf { it.isNotEmpty() }?.toIntOrNull(),
                                                customColumns = sub.getOrNull(9)
                                                    ?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                                                    ?.coerceIn(1, 4),
                                                customHeightDp = sub.getOrNull(10)
                                                    ?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                                                    ?.coerceIn(64, 280),
                                            )
                                        }

                                        "E" -> {
                                            if (sub.size < 3) return@mapNotNull null
                                            FolderItem.SpacerItem(
                                                id = sub[1],
                                                span = runCatching { TileSpan.valueOf(sub[2]) }.getOrDefault(
                                                    TileSpan.BANNER
                                                ),
                                                customColumns = sub.getOrNull(3)
                                                    ?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                                                    ?.coerceIn(1, 4),
                                                customHeightDp = sub.getOrNull(4)
                                                    ?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                                                    ?.coerceIn(24, 320),
                                            )
                                        }

                                        else -> null
                                    }
                                }
                            } ?: emptyList()
                        CanvasBlock.FolderTile(
                            id = parts[1],
                            title = parts[2].ifBlank { "Folder" },
                            apps = apps,
                            span = span,
                            shape = shape,
                            tone = tone,
                            customBgArgb = customBgArgb,
                            customColumns = customColumns,
                            customHeightDp = customHeightDp,
                            layoutMode = layoutMode,
                            gridColumns = gridColumns,
                            items = items,
                        )
                    }

                    else -> null
                }
            }
        }

        private fun String.clean(): String =
            replace(RECORD_SEP, ' ')
                .replace(FIELD_SEP, ' ')
                .replace(ITEM_SEP, ' ')
                .replace(SUBFIELD_SEP, ' ')
    }
}
