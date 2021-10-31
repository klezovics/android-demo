package com.grushevskyi.tr_challenge

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Stock(
    val price: String?,
    val isin: String?
)