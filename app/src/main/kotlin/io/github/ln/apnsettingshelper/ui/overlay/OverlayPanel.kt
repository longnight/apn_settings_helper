package io.github.ln.apnsettingshelper.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import io.github.ln.apnsettingshelper.R

/**
 * Builds the floating panel (classic Views): a draggable header (title + collapse + close) above a
 * hint line and one row per field. Copyable rows get a Copy button wired to [ApnOverlay.PanelSpec.onCopy];
 * dropdown fields render as dimmed "label → value" hints. Pure view construction — the window
 * add/remove and the focusable-toggle clipboard write live in [ApnOverlay].
 */
internal fun buildOverlayPanel(
    context: Context,
    spec: ApnOverlay.PanelSpec,
): View {
    val container =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = panelBackground()
            setPadding(dpToPx(context, PAD_DP), dpToPx(context, HEADER_PAD_DP), dpToPx(context, PAD_DP), dpToPx(context, PAD_DP))
        }
    val list = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    spec.rows.forEach { list.addView(fieldRow(context, it, spec.onCopy)) }
    val body =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(hintView(context))
            addView(
                ScrollView(context).apply {
                    layoutParams =
                        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    addView(list)
                },
            )
        }
    val bar = header(context, spec.title, body, spec.onClose)
    container.addView(bar)
    container.addView(body)
    attachDrag(bar, container, spec.wm, spec.params)
    return container
}

private fun header(
    context: Context,
    title: String,
    body: View,
    onClose: () -> Unit,
): View {
    val bar =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
    val titleView =
        TextView(context).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = TITLE_SP
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
    val collapse = headerButton(context, COLLAPSE_GLYPH, context.getString(R.string.overlay_collapse))
    collapse.setOnClickListener {
        val collapsing = body.visibility != View.GONE
        body.visibility = if (collapsing) View.GONE else View.VISIBLE
        collapse.text = if (collapsing) EXPAND_GLYPH else COLLAPSE_GLYPH
        collapse.contentDescription =
            context.getString(if (collapsing) R.string.overlay_expand else R.string.overlay_collapse)
    }
    val close =
        headerButton(context, CLOSE_GLYPH, context.getString(R.string.overlay_close)).apply {
            setOnClickListener { onClose() }
        }
    bar.addView(titleView)
    bar.addView(collapse)
    bar.addView(close)
    return bar
}

private fun headerButton(
    context: Context,
    label: String,
    desc: String,
): Button =
    Button(context).apply {
        text = label
        contentDescription = desc
        minWidth = 0
        minimumWidth = 0
        setPadding(dpToPx(context, HEADER_BTN_PAD_DP), 0, dpToPx(context, HEADER_BTN_PAD_DP), 0)
    }

private fun hintView(context: Context): TextView =
    TextView(context).apply {
        text = context.getString(R.string.overlay_hint)
        setTextColor(Color.parseColor(HINT_COLOR))
        textSize = HINT_SP
        setPadding(0, dpToPx(context, HINT_TOP_PAD_DP), 0, dpToPx(context, HEADER_PAD_DP))
    }

private fun fieldRow(
    context: Context,
    row: ApnOverlay.Row,
    onCopy: (String, Button) -> Unit,
): View {
    val line =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dpToPx(context, ROW_PAD_DP), 0, dpToPx(context, ROW_PAD_DP))
        }
    val textView =
        TextView(context).apply {
            text = if (row.copyable) "${row.label}: ${row.value}" else "${row.label} → ${row.value}"
            setTextColor(if (row.copyable) Color.WHITE else Color.parseColor(HINT_COLOR))
            textSize = ROW_SP
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
    line.addView(textView)
    if (row.copyable) {
        val copyButton = Button(context)
        copyButton.text = context.getString(R.string.copy)
        copyButton.setOnClickListener { onCopy(row.value, copyButton) }
        line.addView(copyButton)
    }
    return line
}

@SuppressLint("ClickableViewAccessibility")
private fun attachDrag(
    handle: View,
    target: View,
    wm: WindowManager,
    params: WindowManager.LayoutParams,
) {
    var startX = 0
    var startY = 0
    var touchX = 0f
    var touchY = 0f
    handle.setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = params.x
                startY = params.y
                touchX = event.rawX
                touchY = event.rawY
                true
            }

            MotionEvent.ACTION_MOVE -> {
                params.x = startX + (event.rawX - touchX).toInt()
                params.y = startY + (event.rawY - touchY).toInt()
                runCatching { wm.updateViewLayout(target, params) }
                true
            }

            else -> {
                false
            }
        }
    }
}

private fun panelBackground(): GradientDrawable =
    GradientDrawable().apply {
        setColor(Color.parseColor(PANEL_COLOR))
        cornerRadius = CORNER_PX
    }

internal fun dpToPx(
    context: Context,
    value: Float,
): Int = (value * context.resources.displayMetrics.density).toInt()

private const val PAD_DP = 12f
private const val HEADER_PAD_DP = 8f
private const val ROW_PAD_DP = 4f
private const val HINT_TOP_PAD_DP = 2f
private const val HEADER_BTN_PAD_DP = 10f
private const val TITLE_SP = 15f
private const val ROW_SP = 14f
private const val HINT_SP = 12f
private const val CORNER_PX = 24f
private const val HINT_COLOR = "#B0B0B0"
private const val PANEL_COLOR = "#E6202124"
private const val COLLAPSE_GLYPH = "–"
private const val EXPAND_GLYPH = "+"
private const val CLOSE_GLYPH = "✕"
