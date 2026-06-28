package io.github.ln.apnsettingshelper.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import io.github.ln.apnsettingshelper.R

/**
 * Builds the floating panel (classic Views): a draggable header (title + collapse + close) above a
 * hint line, a "Copy these" section of **flat M3-style tap-to-copy chips**, then a "Set these
 * dropdowns" list. Tapping a chip copies its value (via [ApnOverlay.PanelSpec.onCopy]) and marks the
 * chip with a **persistent** ✓; dropdown fields render as dimmed "label → value" hints. Chips wrap
 * via [FlowLayout] — short values pack together, a long value takes its own row (no truncation).
 * Pure view construction — the window add/remove and the focusable-toggle clipboard write live in
 * [ApnOverlay].
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

    val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    val copyRows = spec.rows.filter { it.copyable }
    val dropdownRows = spec.rows.filterNot { it.copyable }

    if (copyRows.isNotEmpty()) {
        content.addView(sectionLabel(context, context.getString(R.string.overlay_copy_section)))
        val chips =
            FlowLayout(context).apply {
                horizontalGapPx = dpToPx(context, CHIP_GAP_DP)
                verticalGapPx = dpToPx(context, CHIP_GAP_DP)
            }
        copyRows.forEach { chips.addView(copyChip(context, it, spec.onCopy)) }
        content.addView(chips)
    }
    if (dropdownRows.isNotEmpty()) {
        content.addView(sectionLabel(context, context.getString(R.string.detail_checklist_section)))
        dropdownRows.forEach { content.addView(dropdownRow(context, it)) }
    }

    val body =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(hintView(context))
            addView(
                ScrollView(context).apply {
                    layoutParams =
                        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    addView(content)
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
): TextView =
    TextView(context).apply {
        text = label
        contentDescription = desc
        setTextColor(Color.WHITE)
        textSize = HEADER_BTN_SP
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        val pad = dpToPx(context, HEADER_BTN_PAD_DP)
        setPadding(pad, pad, pad, pad)
        val mask =
            GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
            }
        background = RippleDrawable(ColorStateList.valueOf(Color.parseColor(CHIP_RIPPLE_COLOR)), null, mask)
    }

private fun hintView(context: Context): TextView =
    TextView(context).apply {
        text = context.getString(R.string.overlay_hint)
        setTextColor(Color.parseColor(HINT_COLOR))
        textSize = HINT_SP
        setPadding(0, dpToPx(context, HINT_TOP_PAD_DP), 0, dpToPx(context, HEADER_PAD_DP))
    }

private fun sectionLabel(
    context: Context,
    text: String,
): TextView =
    TextView(context).apply {
        this.text = text
        setTextColor(Color.parseColor(SECTION_COLOR))
        textSize = SECTION_SP
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dpToPx(context, SECTION_TOP_PAD_DP), 0, dpToPx(context, SECTION_BOTTOM_PAD_DP))
    }

/**
 * One tap-to-copy chip: a flat (no-elevation) rounded container with the field [label][ApnOverlay.Row.label]
 * over its value and a trailing ⧉ glyph. Tapping copies the value and briefly flashes the chip to a
 * ✓ with a stronger fill before reverting. The whole chip is the tap target (no separate button).
 */
private fun copyChip(
    context: Context,
    row: ApnOverlay.Row,
    onCopy: (String) -> Unit,
): View {
    val chip =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = chipBackground(copied = false)
            setPadding(
                dpToPx(context, CHIP_PAD_H_DP),
                dpToPx(context, CHIP_PAD_V_DP),
                dpToPx(context, CHIP_PAD_H_DP),
                dpToPx(context, CHIP_PAD_V_DP),
            )
            isClickable = true
            isFocusable = true
            contentDescription = context.getString(R.string.cd_copy, row.label)
        }
    val label =
        TextView(context).apply {
            text = row.label
            setTextColor(Color.parseColor(CHIP_LABEL_COLOR))
            textSize = CHIP_LABEL_SP
        }
    val valueLine = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
    val value =
        TextView(context).apply {
            text = row.value
            setTextColor(Color.WHITE)
            textSize = CHIP_VALUE_SP
        }
    val glyph =
        TextView(context).apply {
            text = COPY_GLYPH
            setTextColor(Color.parseColor(CHIP_GLYPH_COLOR))
            textSize = CHIP_VALUE_SP
            setPadding(dpToPx(context, CHIP_GLYPH_GAP_DP), 0, 0, 0)
        }
    valueLine.addView(value)
    valueLine.addView(glyph)
    chip.addView(label)
    chip.addView(valueLine)
    chip.setOnClickListener {
        chip.background = chipBackground(copied = true)
        glyph.text = COPIED_GLYPH
        glyph.setTextColor(Color.WHITE)
        onCopy(row.value)
        // Timed flash, then revert to the idle look so the chip doesn't read as a stuck state.
        chip.postDelayed({
            chip.background = chipBackground(copied = false)
            glyph.text = COPY_GLYPH
            glyph.setTextColor(Color.parseColor(CHIP_GLYPH_COLOR))
        }, COPIED_REVERT_MS)
    }
    return chip
}

private fun dropdownRow(
    context: Context,
    row: ApnOverlay.Row,
): View =
    TextView(context).apply {
        text = "${row.label} → ${row.value}"
        setTextColor(Color.parseColor(HINT_COLOR))
        textSize = ROW_SP
        setPadding(0, dpToPx(context, ROW_PAD_DP), 0, dpToPx(context, ROW_PAD_DP))
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

private fun chipBackground(copied: Boolean): Drawable {
    val fill =
        GradientDrawable().apply {
            cornerRadius = CHIP_CORNER_PX
            setColor(Color.parseColor(if (copied) CHIP_COPIED_COLOR else CHIP_COLOR))
        }
    val mask =
        GradientDrawable().apply {
            cornerRadius = CHIP_CORNER_PX
            setColor(Color.WHITE)
        }
    return RippleDrawable(ColorStateList.valueOf(Color.parseColor(CHIP_RIPPLE_COLOR)), fill, mask)
}

private const val PAD_DP = 12f
private const val HEADER_PAD_DP = 8f
private const val ROW_PAD_DP = 4f
private const val HINT_TOP_PAD_DP = 2f
private const val HEADER_BTN_PAD_DP = 10f
private const val SECTION_TOP_PAD_DP = 10f
private const val SECTION_BOTTOM_PAD_DP = 4f
private const val CHIP_GAP_DP = 6f
private const val CHIP_PAD_H_DP = 12f
private const val CHIP_PAD_V_DP = 8f
private const val CHIP_GLYPH_GAP_DP = 8f
private const val TITLE_SP = 15f
private const val HEADER_BTN_SP = 18f
private const val ROW_SP = 14f
private const val HINT_SP = 12f
private const val SECTION_SP = 11f
private const val CHIP_LABEL_SP = 11f
private const val CHIP_VALUE_SP = 14f
private const val CORNER_PX = 24f
private const val CHIP_CORNER_PX = 16f
private const val HINT_COLOR = "#B0B0B0"
private const val PANEL_COLOR = "#E6202124"
private const val SECTION_COLOR = "#4FD8D2"
private const val CHIP_COLOR = "#2B4D4A"
private const val CHIP_COPIED_COLOR = "#0F6E6B"
private const val CHIP_LABEL_COLOR = "#9FB6B5"
private const val CHIP_GLYPH_COLOR = "#4FD8D2"
private const val CHIP_RIPPLE_COLOR = "#33FFFFFF"
private const val COLLAPSE_GLYPH = "–"
private const val EXPAND_GLYPH = "+"
private const val CLOSE_GLYPH = "✕"
private const val COPY_GLYPH = "⧉"
private const val COPIED_GLYPH = "✓"
private const val COPIED_REVERT_MS = 1500L
