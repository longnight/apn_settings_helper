package io.github.ln.apnsettingshelper

import android.app.Application

/** Holds the [AppGraph] for the process lifetime. Registered in the manifest. */
class ApnApplication : Application() {
    val graph: AppGraph by lazy { AppGraph(this) }
}
