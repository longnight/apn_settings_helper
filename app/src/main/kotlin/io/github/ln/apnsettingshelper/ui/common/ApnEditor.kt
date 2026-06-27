package io.github.ln.apnsettingshelper.ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Open the system APN editor. Some OEMs block [Settings.ACTION_APN_SETTINGS] for non-system
 * apps; on failure we fall back to the wireless/network settings screen. Returns `false`
 * only if even the fallback is unavailable, so the caller can show guidance.
 */
fun openApnEditor(context: Context): Boolean {
    val targets =
        listOf(
            Intent(Settings.ACTION_APN_SETTINGS),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
        )
    for (intent in targets) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return true
        } catch (_: ActivityNotFoundException) {
            // try the next fallback
        }
    }
    return false
}
