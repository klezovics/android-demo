package com.grushevskyi.tr_challenge

class StockDataProvider {

    companion object {
        private val STOCK_NAME_TO_ISIN_MAP: Map<String, String> = mapOf(
            "Apple" to "US0378331005",
            "Adidas" to "DE000A1EWWW0",
            "Airbus" to "NL0000235190",
            "Allianz" to "DE0008404005",
            "Alphabet" to "US02079K3059",
            "Amazon" to "US0231351067",
            "Axa" to "FR0000120628",
            "Bank of America" to "US0605051046",
            "Bayer" to "DE000BAY0017",
            "Berkshire" to "US0846707026"
        )

        private val STOCK_NAMES_ORDERED =
            STOCK_NAME_TO_ISIN_MAP.entries.sortedBy { it.key }.map { e -> e.key }

        private val STOCK_ISIN_ORDERED =
            STOCK_NAME_TO_ISIN_MAP.entries.sortedBy { it.key }.map { e -> e.value }

        fun getAllStockNames(): List<String> {
            return STOCK_NAMES_ORDERED
        }

        fun getAllStockIsin(): List<String> {
            return STOCK_ISIN_ORDERED
        }
    }
}