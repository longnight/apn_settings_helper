package io.github.ln.apnsettingshelper.ui.nav

/** App navigation routes. */
object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{presetId}"
    const val ARG_PRESET_ID = "presetId"

    fun detail(presetId: String): String = "detail/$presetId"
}
