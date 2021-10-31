package com.grushevskyi.tr_grushevskyi1

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
//Stock
data class Stocks(
    val price: String?,
    val isin: String?
)