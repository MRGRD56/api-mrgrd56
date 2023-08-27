package ru.mrgrd56.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ConflictException : ResponseStatusException {
    constructor() : super(HttpStatus.CONFLICT)
    constructor(reason: String?) : super(HttpStatus.CONFLICT, reason)
    constructor(reason: String?, cause: Throwable?) : super(HttpStatus.CONFLICT, reason, cause)
}
