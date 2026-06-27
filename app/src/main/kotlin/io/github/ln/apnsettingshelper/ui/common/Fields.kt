package io.github.ln.apnsettingshelper.ui.common

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import io.github.ln.apnsettingshelper.R

/**
 * One copyable APN field: an overline [label] above the literal [value], with a Copy button
 * that puts the value on the clipboard (and confirms with a toast). Used for every non-empty
 * non-dropdown field, so the user pastes value-by-value into the system APN editor.
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
    ListItem(
        overlineContent = { Text(label) },
        headlineContent = { Text(value, style = MaterialTheme.typography.bodyLarge) },
        trailingContent = {
            TextButton(
                onClick = {
                    clipboard.setText(AnnotatedString(value))
                    Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.semantics { contentDescription = copyDescription },
            ) {
                Text(stringResource(R.string.copy))
            }
        },
        modifier = modifier,
    )
}

/**
 * One dropdown-field instruction rendered as a checklist item ("Set <field> to <X>"), with a
 * checkbox the user can tick to track progress. The checked state is ephemeral UI progress —
 * not persisted — because the app can't verify the device's actual APN.
 */
@Composable
fun ChecklistItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingContent = { Checkbox(checked = checked, onCheckedChange = onCheckedChange) },
        headlineContent = { Text(text) },
        modifier = modifier.clickable { onCheckedChange(!checked) },
    )
}
