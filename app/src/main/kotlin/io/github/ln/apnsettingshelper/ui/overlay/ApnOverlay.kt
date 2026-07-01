package io.github.ln.apnsettingshelper.ui.overlay

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * Overlay-tier controller (AGENTS.md host option #1: no service). Adds/removes the floating panel
 * built by [buildOverlayPanel] to the application `WindowManager`, and owns the per-copy clipboard
 * write that makes Copy work over the system APN editor.
 *
 * Clipboard write strategy: Android 10+/MIUI block `setPrimaryClip` from a background app. Per Copy we
 * first try a *silent* write from a fresh 1x1 focusable overlay window ([writeViaProxy]) — enough on
 * lenient devices. On MIUI only the first write after the app was foreground sticks (verified on-device:
 * every later Copy is dropped despite confirmed window focus — neither fresh windows, focus, nor waiting
 * helps), so we read the clip back to check; if it didn't stick we write as the genuine foreground app
 * via [ClipboardWriteActivity] (a brief, reliable flash). Feedback is the tapped chip flashing to a
 * ✓ for a moment (built in [buildOverlayPanel]) because MIUI also suppresses our toasts.
 */
object ApnOverlay {
    /** One field line in the panel; [copyable] text fields become tap-to-copy chips, dropdowns don't. */
    data class Row(
        val label: String,
        val value: String,
        val copyable: Boolean,
    )

    /** Inputs the [buildOverlayPanel] view builder needs (bundled to keep its signature small). */
    class PanelSpec(
        val title: String,
        val rows: List<Row>,
        val wm: WindowManager,
        val params: WindowManager.LayoutParams,
        val onCopy: (value: String) -> Unit,
        val onClose: () -> Unit,
    )

    private var panel: View? = null
    private var wm: WindowManager? = null
    private var params: WindowManager.LayoutParams? = null

    val isShowing: Boolean get() = panel != null

    fun canDraw(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun permissionIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))

    fun show(
        context: Context,
        title: String,
        rows: List<Row>,
    ) {
        val app = context.applicationContext
        if (!canDraw(app)) return
        hide(app)
        val windowManager = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val lp = layoutParams(app)
        wm = windowManager
        params = lp
        val spec =
            PanelSpec(
                title = title,
                rows = rows,
                wm = windowManager,
                params = lp,
                onCopy = { value -> copy(app, value) },
                onClose = { hide(app) },
            )
        val view = buildOverlayPanel(panelContext(app, context), spec)
        windowManager.addView(view, lp)
        panel = view
    }

    /**
     * The app context re-localized to match the in-app language carried by [source] (the caller's
     * `LocalContext`, already locale-overridden), so the panel's own strings + layout direction follow
     * the user's pick instead of the system locale. Built from the app context (not [source]) so the
     * long-lived overlay never holds an Activity reference.
     */
    private fun panelContext(
        app: Context,
        source: Context,
    ): Context {
        val config = Configuration(app.resources.configuration)
        config.setLocale(source.resources.configuration.locales[0])
        return app.createConfigurationContext(config)
    }

    fun hide(context: Context) {
        val current = panel ?: return
        val windowManager = wm ?: context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        runCatching { windowManager.removeView(current) }
        panel = null
        wm = null
        params = null
    }

    private fun layoutParams(context: Context): WindowManager.LayoutParams =
        WindowManager
            .LayoutParams(
                dpToPx(context, WIDTH_DP),
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = dpToPx(context, MARGIN_DP)
                y = dpToPx(context, TOP_DP)
            }

    /**
     * Copy [value] to the clipboard. Add a 1x1 invisible focusable window and write once it gains
     * focus — enough on lenient devices; [writeViaProxy] then verifies and, when the OS dropped the
     * write (MIUI after its first), falls back to the foreground writer. The main panel stays
     * pass-through and untouched; the silent path is just a brief focus blip (no app-switch).
     */
    private fun copy(
        context: Context,
        value: String,
    ) {
        val app = context.applicationContext
        val windowManager = wm
        if (windowManager == null) {
            writeClip(app, value)
            return
        }
        val proxy = View(app)
        val lp =
            WindowManager
                .LayoutParams(
                    1,
                    1,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT,
                ).apply { gravity = Gravity.TOP or Gravity.START }
        if (runCatching { windowManager.addView(proxy, lp) }.isFailure) {
            writeClip(app, value)
            return
        }
        writeViaProxy(windowManager, proxy, app, value, attempt = 0)
    }

    /**
     * Write the clip once the per-copy [proxy] window gains focus, verify it stuck (read back while
     * still focused), then remove the window. Polls [hasWindowFocus][View.hasWindowFocus] every
     * [FOCUS_POLL_MS] up to [MAX_FOCUS_POLLS] (focus lands a frame or two after the add). If the write
     * didn't stick (MIUI blocks background writes after the first), fall back to [ClipboardWriteActivity].
     */
    private fun writeViaProxy(
        windowManager: WindowManager,
        proxy: View,
        context: Context,
        value: String,
        attempt: Int,
    ) {
        if (!proxy.hasWindowFocus() && attempt < MAX_FOCUS_POLLS) {
            proxy.postDelayed({ writeViaProxy(windowManager, proxy, context, value, attempt + 1) }, FOCUS_POLL_MS)
            return
        }
        // Write, then verify it stuck by reading back while the proxy still holds focus. MIUI silently
        // drops background writes after the first, so a failed read-back means we fall back to writing
        // as the foreground app via [ClipboardWriteActivity] (reliable, at the cost of a brief flash).
        writeClip(context, value)
        val stuck = clipboardHolds(context, value)
        runCatching { windowManager.removeView(proxy) }
        if (!stuck) {
            runCatching { context.startActivity(ClipboardWriteActivity.intent(context, value)) }
        }
    }

    /** True if the primary clip currently equals [value]; false if it differs or can't be read. */
    private fun clipboardHolds(
        context: Context,
        value: String,
    ): Boolean =
        runCatching {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .primaryClip
                ?.getItemAt(0)
                ?.text
                ?.toString() == value
        }.getOrDefault(false)

    private fun writeClip(
        context: Context,
        value: String,
    ) {
        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cb.setPrimaryClip(ClipData.newPlainText("APN", value))
    }

    private const val WIDTH_DP = 300f
    private const val MARGIN_DP = 12f
    private const val TOP_DP = 80f
    private const val FOCUS_POLL_MS = 25L
    private const val MAX_FOCUS_POLLS = 16
}
