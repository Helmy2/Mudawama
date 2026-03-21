package io.github.helmy2.mudawama.core.data.networking

import co.touchlab.kermit.Logger
import io.github.helmy2.mudawama.core.domain.MudawamaLogger

class KermitLogger : MudawamaLogger {
    override fun debug(tag: String, message: String) {
        Logger.d(tag = tag) { message }
    }

    override fun info(tag: String, message: String) {
        Logger.i(tag = tag) { message }
    }

    override fun warn(tag: String, message: String) {
        Logger.w(tag = tag) { message }
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        Logger.e(throwable = throwable, tag = tag) { message }
    }
}
