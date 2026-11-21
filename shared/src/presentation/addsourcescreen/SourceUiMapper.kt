package com.jjcr11.rss.presentation.addsourcescreen

import com.jjcr11.rss.domain.model.Source

fun Source.toUiModel(): SourceUiModel = SourceUiModel(
    id = this.id.toString(),
    name = this.name,
    url = this.webUrl,
)
