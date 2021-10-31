package com.grushevskyi.tr_challenge

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.grushevskyi.tr_challenge.databinding.ActivityMainBinding
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.SocketFactory


class MainActivity : AppCompatActivity() {

    private var rvAdapter: RVAdapter? = null
    private val stockPriceList = MutableList(STOCK_ISIN_ORDERED.size) { "" }
    private val jsonAdapter: JsonAdapter<Stock>
    private val webSocketClient: WebSocketClient

    private lateinit var binding: ActivityMainBinding

    init {
        jsonAdapter = getMoshi().adapter(Stock::class.java)
        webSocketClient = createWebSocketClient(URI(TRADE_REPUBLIC_WEB_SOCKET_URL))
    }

    private fun getMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onPause() {
        super.onPause()
        webSocketClient.close()
    }

    override fun onResume() {
        super.onResume()
        initRV()
        initWebSocket()
    }

    private fun initRV() {
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = rvAdapter
    }

    private fun initWebSocket() {
        webSocketClient.setSocketFactory(SocketFactory.getDefault())
        webSocketClient.connect()
    }

    private fun createWebSocketClient(traderepublicUri: URI): WebSocketClient {
        return object : WebSocketClient(traderepublicUri) {

            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "Opening connection...")
                subscribeToSocket()
            }

            override fun onMessage(message: String?) {
                Log.d(TAG, "Current data: $message")
                setUpStockPriceText(message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "Closing connection...")
                unsubscribeFromSocket()
            }

            override fun onError(ex: Exception?) {
                Log.e(TAG, "Something went wrong: ${ex?.localizedMessage}")
            }
        }
    }

    private fun subscribeToSocket() {
        for (isin in STOCK_ISIN_ORDERED) {
            webSocketClient.send("{\"subscribe\":\"$isin\"}")
        }
    }

    private fun unsubscribeFromSocket() {
        for (isin in STOCK_ISIN_ORDERED) {
            webSocketClient.send("{\"unsubscribe\":\"$isin\"}")
        }
    }

    private fun setUpStockPriceText(message: String?) {
        message?.let {

            val stock = jsonAdapter.fromJson(message)
            val stockPriceStr = String.format("%.2f", stock?.price?.toDouble())

            if (stock != null && STOCK_ISIN_ORDERED.indexOf(stock.isin) != -1) {
                stockPriceList.set(
                    STOCK_ISIN_ORDERED.indexOf(stock.isin),
                    stockPriceStr + " \u20ac"
                )
            }

            val observable = Observable.create<ArrayList<String>> {
                it.onNext(ArrayList(stockPriceList))
            }

            observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    rvAdapter = RVAdapter(ArrayList(stockPriceList))
                    binding.rvList.adapter = rvAdapter
                },
                    { it.localizedMessage }
                )
        }
    }

    companion object {
        const val TRADE_REPUBLIC_WEB_SOCKET_URL = "ws://159.89.15.214:8080/"
        const val TAG = "Trade Republic"
        val STOCK_ISIN_ORDERED: ArrayList<String> = ArrayList(StockDataProvider.getAllStockIsin())
    }
}
