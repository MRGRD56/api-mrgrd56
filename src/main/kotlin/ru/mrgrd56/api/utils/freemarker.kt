package ru.mrgrd56.api.utils

import freemarker.template.Template
import java.io.StringWriter

fun Template.fill(model: Any): String {
    val stringWriter = StringWriter()
    this.process(model, stringWriter)
    return stringWriter.toString()
}