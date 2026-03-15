package com.emagioda.myapp.domain.model

class AssetContentException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause)
