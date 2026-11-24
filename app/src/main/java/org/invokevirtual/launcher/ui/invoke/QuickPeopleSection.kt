package org.invokevirtual.launcher.ui.invoke

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.invokevirtual.launcher.data.CanvasBlock
import org.invokevirtual.launcher.data.QuickContact
import org.invokevirtual.launcher.data.TileSpan
import org.invokevirtual.launcher.ui.theme.InvokeIcons

/**
 * Compact header row above pinned Quick People with an inline "Add" action launching Android 17's
 * zero-permission Contacts Picker.
 */
@Composable
internal fun QuickPeopleHeaderRow(
    count: Int,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Quick People",
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }

        FilledTonalButton(
            onClick = onAddClick,
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Icon(
                imageVector = InvokeIcons.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add",
                style = MaterialTheme.typography.labelLargeEmphasized,
                maxLines = 1,
            )
        }
    }
}

/**
 * Authentic Material 3 Expressive 1.5.0 [SegmentedListItem] contact row with 1-tap Call
 * (`ACTION_DIAL`) and Message (`ACTION_SENDTO`) actions, plus long-click options sheet.
 */
@Composable
internal fun QuickContactRow(
    contact: QuickContact,
    index: Int,
    count: Int,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onLongClick: () -> Unit,
    isPinned: Boolean = false,
    cardAlpha: Float = 1f,
) {
    val haptic = LocalHapticFeedback.current

    SegmentedListItem(
        onClick = onMessage,
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onLongClick()
        },
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = cardAlpha),
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Options for ${contact.displayName}") {
                        onLongClick()
                        true
                    },
                )
            },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = contact.initials,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    maxLines = 1,
                )
            }
        },
        supportingContent = {
            Text(
                text = if (isPinned) "${contact.phoneNumber} · On Home" else contact.phoneNumber,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onMessage,
                    modifier = Modifier.size(50.dp),
                ) {
                    Icon(
                        imageVector = InvokeIcons.Chat,
                        contentDescription = "Message ${contact.displayName}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
                IconButton(
                    onClick = onCall,
                    modifier = Modifier.size(50.dp),
                ) {
                    Icon(
                        imageVector = InvokeIcons.Phone,
                        contentDescription = "Call ${contact.displayName}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        },
        content = {
            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.titleMediumEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

/**
 * M3 Expressive action sheet for Quick People & pinned ContactTiles: Call, Message,
 * Pin/Unpin/Customize on Home Canvas, and Remove from Quick People.
 */
@Composable
fun QuickContactActionSheet(
    contact: QuickContact,
    pinnedBlock: CanvasBlock.ContactTile?,
    isInQuickPeople: Boolean,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onPinToHome: (TileSpan) -> Unit,
    onCustomizeOnHome: () -> Unit,
    onUnpinFromHome: () -> Unit,
    onRemoveFromQuickPeople: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = contact.initials,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.displayName,
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (pinnedBlock != null) {
                            "${contact.phoneNumber} · On Home (${pinnedBlock.sizeLabel})"
                        } else {
                            contact.phoneNumber
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                SegmentedListItem(
                    onClick = {
                        onDismiss()
                        onCall()
                    },
                    shapes = ListItemDefaults.segmentedShapes(0, 2),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = InvokeIcons.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    content = {
                        Text(
                            text = "Call ${contact.displayName}",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
                SegmentedListItem(
                    onClick = {
                        onDismiss()
                        onMessage()
                    },
                    shapes = ListItemDefaults.segmentedShapes(1, 2),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = InvokeIcons.Chat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    content = {
                        Text(
                            text = "Message ${contact.displayName}",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }

            Text(
                text = "Home Canvas",
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp),
            )

            if (pinnedBlock == null) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(TileSpan.entries, key = { it.name }) { span ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                onPinToHome(span)
                                onDismiss()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = InvokeIcons.Pin,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            label = {
                                Text(
                                    text = "Pin ${span.label}",
                                    style = MaterialTheme.typography.labelLargeEmphasized,
                                    maxLines = 1,
                                )
                            },
                            modifier = Modifier.heightIn(min = 42.dp),
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    SegmentedListItem(
                        onClick = {
                            onDismiss()
                            onCustomizeOnHome()
                        },
                        shapes = ListItemDefaults.segmentedShapes(0, 2),
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        leadingContent = {
                            Icon(
                                imageVector = InvokeIcons.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        content = {
                            Text(
                                text = "Customize tile on Home",
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                    SegmentedListItem(
                        onClick = {
                            onUnpinFromHome()
                            onDismiss()
                        },
                        shapes = ListItemDefaults.segmentedShapes(1, 2),
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        leadingContent = {
                            Icon(
                                imageVector = InvokeIcons.Pin,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        content = {
                            Text(
                                text = "Remove from Home",
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }

            if (isInQuickPeople) {
                SegmentedListItem(
                    onClick = {
                        onRemoveFromQuickPeople()
                        onDismiss()
                    },
                    shapes = ListItemDefaults.segmentedShapes(0, 1),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = InvokeIcons.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    content = {
                        Text(
                            text = "Remove from Quick People",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}

/**
 * Low-profile M3E pill card inviting the user to pick favorite contacts via Android 17's
 * privacy-first Contacts Picker (no `READ_CONTACTS` permission required).
 */
@Composable
internal fun QuickPeoplePickerBanner(
    subtitle: String,
    onPickClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardAlpha: Float = 1f,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = cardAlpha),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = InvokeIcons.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Quick People",
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            FilledTonalButton(
                onClick = onPickClick,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(
                    text = "Pick",
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    maxLines = 1,
                )
            }
        }
    }
}
