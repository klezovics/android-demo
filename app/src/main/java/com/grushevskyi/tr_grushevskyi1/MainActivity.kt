package com.grushevskyi.tr_grushevskyi1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.grushevskyi.tr_grushevskyi1.databinding.ActivityMainBinding
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
    private val stockList = MutableList(STOCK_ISIN_ORDERED.size) { "" }

    private lateinit var webSocketClient: WebSocketClient
    private lateinit var binding: ActivityMainBinding

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
        createWebSocketClient(URI(TRADE_REPUBLIC_WEB_SOCKET_URL))

        webSocketClient.setSocketFactory(SocketFactory.getDefault())
        webSocketClient.connect()
    }

    private fun createWebSocketClient(traderepublicUri: URI) {
        webSocketClient = object : WebSocketClient(traderepublicUri) {

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

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter: JsonAdapter<Stock> = moshi.adapter(Stock::class.java)

            val stockRaw = adapter.fromJson(message)
            val stock = String.format("%.2f", stockRaw?.price?.toDouble())

            if (stockRaw != null && STOCK_ISIN_ORDERED.indexOf(stockRaw.isin) != -1) {
                stockList.set(STOCK_ISIN_ORDERED.indexOf(stockRaw.isin), stock + " \u20ac")
            }

            val observable = Observable.create<ArrayList<String>> {
                it.onNext(ArrayList(stockList))
            }

            observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    rvAdapter = RVAdapter(ArrayList(stockList))
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
