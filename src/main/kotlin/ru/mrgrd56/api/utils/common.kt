package ru.mrgrd56.api.utils

inline infix fun <T> T?.ifNull(action: () -> Unit) {
    if (this == null) {
        action()
    }
}

inline infix fun <C, R> C?.ifNullOrBlank(defaultValue: () -> R): R where C : R, R : CharSequence =
    if (isNullOrBlank()) defaultValue() else this
