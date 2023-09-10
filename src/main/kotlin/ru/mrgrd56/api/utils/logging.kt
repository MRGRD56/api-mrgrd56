package ru.mrgrd56.api.utils

import dev.b37.mgutils.logging.ScopedLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun logger(clazz: Class<*>): Logger {
    return LoggerFactory.getLogger(clazz)
}

fun Logger.scoped() = ScopedLogger.of(this)

fun Logger.scoped(name: String?) = ScopedLogger.of(this, name)

fun Logger.scoped(name: String?, scopeId: Any?) = ScopedLogger.of(this, name, scopeId)