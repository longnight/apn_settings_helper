package io.github.ln.apnsettingshelper.ui.overlay

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * One-shot, invisible Activity whose only job is to write the clipboard as the **foreground** app.
 *
 * This is [ApnOverlay]'s reliable fallback: MIUI (and Android 10+ in general) block `setPrimaryClip`
 * from a background window — on MIUI only the first write after the app was last foreground sticks,
 * so the overlay's silent per-copy write fails for every later Copy. A genuine foreground Activity is
 * unconditionally allowed to write, so when the silent write is detected to have failed the overlay
 * launches this; it writes [EXTRA_VALUE] in [onCreate] and finishes immediately. The translucent,
 * no-animation theme + its own empty task affinity keep it from flashing UI or surfacing the main app.
 */
class ClipboardWriteActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.getStringExtra(EXTRA_VALUE)?.let { value ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("APN", value))
        }
        finish()
    }

    companion object {
        private const val EXTRA_VALUE = "value"

        /** Intent that writes [value] to the clipboard, launchable from a non-Activity context. */
        fun intent(
            context: Context,
            value: String,
        ): Intent =
            Intent(context, ClipboardWriteActivity::class.java)
                .putExtra(EXTRA_VALUE, value)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
}
