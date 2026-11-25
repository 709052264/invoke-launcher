package org.invokevirtual.launcher.ui.invoke

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.invokevirtual.launcher.data.HomeCanvasState
import org.invokevirtual.launcher.data.HomeCanvasStore
import org.invokevirtual.launcher.data.QuickContact
import org.invokevirtual.launcher.data.QuickContactsStore
import org.invokevirtual.launcher.data.SettingsStore
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.AppShortcutItem
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.system.PrivateSpaceState
import kotlin.time.Duration.Companion.milliseconds

enum class InvokeFilter(val label: String) {
    ALL("All"),
    PINNED("Pinned"),
    WORK("Work"),
}

@Immutable
data class InvokeUiState(
    val query: String = "",
    val activeFilter: InvokeFilter = InvokeFilter.ALL,
    val hasWorkApps: Boolean = false,
    val isSearching: Boolean = false,
    val rankedSearchResults: List<InstalledApp> = emptyList(),
    val sections: List<LetterSection> = emptyList(),
    val pinnedDrawerApps: List<InstalledApp> = emptyList(),
    val pinnedDrawerKeys: Set<AppKey> = emptySet(),
    val matchingShortcuts: List<AppShortcutItem> = emptyList(),
    val quickContacts: List<QuickContact> = emptyList(),
    val matchingContacts: List<QuickContact> = emptyList(),
    val showContactPickerPromptInSearch: Boolean = false,
    val mathResult: MathEvalResult? = null,
    val homeCanvas: HomeCanvasState = HomeCanvasState(),
    val privateSpace: PrivateSpaceState = PrivateSpaceState.Unavailable,
) {
    val topMatch: InstalledApp?
        get() = if (isSearching) rankedSearchResults.firstOrNull() else null
}

/**
 * Lightweight ViewModel dedicated specifically to Invoke Sheet search & filtering so ephemeral
 * query text and debounced shortcut lookups survive foldable unfolds and config changes cleanly.
 */
@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class InvokeViewModel(
    private val appCatalog: AppCatalog,
    homeCanvasStore: HomeCanvasStore,
    private val quickContactsStore: QuickContactsStore,
    settingsStore: SettingsStore,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _filter = MutableStateFlow(InvokeFilter.ALL)

    private val shortcutMatches: StateFlow<List<AppShortcutItem>> = _query
        .debounce(110.milliseconds)
        .distinctUntilChanged()
        .mapLatest { q -> appCatalog.searchShortcuts(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val pinnedDrawerKeysFlow: StateFlow<List<AppKey>> = settingsStore.settings
        .map { it.pinnedDrawerApps }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<InvokeUiState> = combine(
        _query,
        _filter,
        appCatalog.apps,
        homeCanvasStore.canvasState,
        shortcutMatches,
        appCatalog.privateSpace,
        quickContactsStore.contacts,
        pinnedDrawerKeysFlow,
    ) { values ->
        val query = values[0] as String
        val requestedFilter = values[1] as InvokeFilter
        @Suppress("UNCHECKED_CAST")
        val allApps = values[2] as List<InstalledApp>
        val homeCanvas = values[3] as HomeCanvasState
        @Suppress("UNCHECKED_CAST")
        val shortcuts = values[4] as List<AppShortcutItem>
        val privateSpace = values[5] as PrivateSpaceState
        @Suppress("UNCHECKED_CAST")
        val quickContacts = values[6] as List<QuickContact>

        @Suppress("UNCHECKED_CAST")
        val pinnedDrawerOrder = values[7] as List<AppKey>

        val appsByKey = allApps.associateBy { it.key }
        val pinnedDrawerApps = pinnedDrawerOrder.mapNotNull { appsByKey[it] }
        val pinnedDrawerKeys = pinnedDrawerApps.map { it.key }.toSet()

        val hasWorkApps = allApps.any { it.isWorkProfile }
        val activeFilter = if (requestedFilter == InvokeFilter.WORK && !hasWorkApps) {
            InvokeFilter.ALL
        } else {
            requestedFilter
        }

        val filteredByCategory = when (activeFilter) {
            InvokeFilter.ALL -> allApps
            InvokeFilter.PINNED -> pinnedDrawerApps
            InvokeFilter.WORK -> allApps.filter { it.isWorkProfile }
        }

        val trimmed = query.trim()
        val isSearching = trimmed.isNotEmpty()
        val ranked = if (isSearching) {
            InvokeSearchEngine.rankApps(trimmed, filteredByCategory)
        } else {
            emptyList()
        }
        val sections = if (isSearching) {
            emptyList()
        } else {
            InvokeSearchEngine.groupAppsByLetter(filteredByCategory)
        }
        val matchingContacts = if (isSearching) {
            InvokeSearchEngine.rankContacts(trimmed, quickContacts)
        } else {
            emptyList()
        }
        val contactIntent = isSearching && InvokeSearchEngine.isContactIntentQuery(trimmed)

        InvokeUiState(
            query = query,
            activeFilter = activeFilter,
            hasWorkApps = hasWorkApps,
            isSearching = isSearching,
            rankedSearchResults = ranked,
            sections = sections,
            pinnedDrawerApps = pinnedDrawerApps,
            pinnedDrawerKeys = pinnedDrawerKeys,
            matchingShortcuts = if (isSearching) shortcuts else emptyList(),
            quickContacts = quickContacts,
            matchingContacts = matchingContacts,
            showContactPickerPromptInSearch = contactIntent,
            mathResult = if (isSearching) InvokeSearchEngine.tryEvaluateMath(trimmed) else null,
            homeCanvas = homeCanvas,
            privateSpace = privateSpace,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvokeUiState())

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun selectFilter(filter: InvokeFilter) {
        _filter.value = filter
    }

    fun togglePrivateSpace(lock: Boolean) {
        appCatalog.setPrivateSpaceLocked(lock)
    }

    fun savePickedContacts(contacts: List<QuickContact>) {
        if (contacts.isEmpty()) return
        viewModelScope.launch {
            quickContactsStore.upsertContacts(contacts)
        }
    }

    fun removeQuickContact(lookupKey: String) {
        viewModelScope.launch {
            quickContactsStore.removeContact(lookupKey)
        }
    }

    class Factory(
        private val appCatalog: AppCatalog,
        private val homeCanvasStore: HomeCanvasStore,
        private val quickContactsStore: QuickContactsStore,
        private val settingsStore: SettingsStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InvokeViewModel(appCatalog, homeCanvasStore, quickContactsStore, settingsStore) as T
    }
}
