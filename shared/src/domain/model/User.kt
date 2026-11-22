package com.jjcr11.rss.domain.model

import kotlin.uuid.Uuid

data class User(
    val id: Uuid,
    val email: String?,
)
