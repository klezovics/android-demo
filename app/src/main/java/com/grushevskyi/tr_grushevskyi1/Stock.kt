package com.grushevskyi.tr_grushevskyi1

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Stock(
    val price: String?,
    val isin: String?
)