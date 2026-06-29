package io.github.ln.apnsettingshelper.ui.common

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.ln.apnsettingshelper.R
import kotlinx.coroutines.delay

/**
 * One copyable APN field, shown as an M3 [ElevatedCard]: an overline [label] above the literal
 * [value]. **Tapping anywhere on the card** copies the value to the clipboard (confirmed with a
 * toast); the trailing `⧉` glyph flips to `✓` for ~1.5s as feedback, then reverts — matching the
 * float overlay. The inner [ListItem] is transparent so the card's elevated surface shows through.
 */
@Composable
fun CopyableField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedMessage = stringResource(R.string.copied, label)
    val copyDescription = stringResource(R.string.cd_copy, label)
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(COPY_REVERT_MS)
            copied = false
        }
    }
    ElevatedCard(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable(onClickLabel = copyDescription) {
                    clipboard.setText(AnnotatedString(value))
                    Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                    copied = true
                },
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            overlineContent = { Text(label) },
            headlineContent = { Text(value, style = MaterialTheme.typography.bodyLarge) },
            trailingContent = {
                Text(
                    text = if (copied) COPIED_GLYPH else COPY_GLYPH,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                )
            },
        )
    }
}

/**
 * One dropdown-field instruction rendered as a checklist item ("Set <field> to <X>") inside an
 * M3 [ElevatedCard], with a checkbox the user can tick to track progress. The checked state is
 * ephemeral UI progress — not persisted — because the app can't verify the device's actual APN.
 */
@Composable
fun ChecklistItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = { Checkbox(checked = checked, onCheckedChange = onCheckedChange) },
            headlineContent = { Text(text) },
            modifier = Modifier.clickable { onCheckedChange(!checked) },
        )
    }
}

/**
 * The detail screen's top "Notes" body: the preset's line/plan designation (e.g. "Type D / Docomo")
 * merged with any freeform [notes] into one area. The [line] reads as the prominent first line;
 * [notes] follow in a muted tone. Either may be blank (the caller only shows this when one is set).
 */
@Composable
fun PresetNotes(
    line: String,
    notes: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        if (line.isNotBlank()) {
            Text(text = line, style = MaterialTheme.typography.bodyMedium)
        }
        if (notes.isNotBlank()) {
            if (line.isNotBlank()) Spacer(Modifier.height(8.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val COPY_GLYPH = "⧉"
private const val COPIED_GLYPH = "✓"
private const val COPY_REVERT_MS = 1500L
