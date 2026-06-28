package io.github.ln.apnsettingshelper.ui.overlay

import android.content.Context
import android.view.ViewGroup

/** dp → px for the overlay's hand-built views (shared by [buildOverlayPanel] and [ApnOverlay]). */
internal fun dpToPx(
    context: Context,
    value: Float,
): Int = (value * context.resources.displayMetrics.density).toInt()

/**
 * Minimal wrapping container (no AndroidX/flexbox dependency): lays children left-to-right and wraps
 * to a new line when the next child would overflow the available width. A child wider than the row
 * is clamped to the full width (so a long chip takes its own row). [horizontalGapPx]/[verticalGapPx]
 * space the children.
 */
internal class FlowLayout(
    context: Context,
) : ViewGroup(context) {
    var horizontalGapPx = 0
    var verticalGapPx = 0

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val available = width - paddingLeft - paddingRight
        val childWidthSpec = MeasureSpec.makeMeasureSpec(available, MeasureSpec.AT_MOST)
        val childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        var x = 0
        var y = 0
        var rowHeight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            child.measure(childWidthSpec, childHeightSpec)
            if (x > 0 && x + child.measuredWidth > available) {
                x = 0
                y += rowHeight + verticalGapPx
                rowHeight = 0
            }
            x += child.measuredWidth + horizontalGapPx
            rowHeight = maxOf(rowHeight, child.measuredHeight)
        }
        val height = y + rowHeight + paddingTop + paddingBottom
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec))
    }

    override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int,
    ) {
        val available = (r - l) - paddingLeft - paddingRight
        var x = paddingLeft
        var y = paddingTop
        var rowHeight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            if (x > paddingLeft && x + child.measuredWidth > paddingLeft + available) {
                x = paddingLeft
                y += rowHeight + verticalGapPx
                rowHeight = 0
            }
            child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
            x += child.measuredWidth + horizontalGapPx
            rowHeight = maxOf(rowHeight, child.measuredHeight)
        }
    }
}
