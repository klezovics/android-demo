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

    private lateinit var binding: ActivityMainBinding
    private var rvAdapter: RVAdapter? = null
    private lateinit var webSocketClient: WebSocketClient
    private val stockList = MutableList<String>(listOfMyISIN.size){""}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        initRV()
        initWebSocket()
    }

    override fun onPause() {
        super.onPause()
        webSocketClient.close()
    }

    private fun initRV() {
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = rvAdapter
    }

    private fun initWebSocket() {
        val traderepublicUri: URI? = URI(WEB_SOCKET_URL)

        createWebSocketClient(traderepublicUri)

        val socketFactory = SocketFactory.getDefault() as SocketFactory
        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }

    private fun createWebSocketClient(traderepublicUri: URI?) {
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
        for (isin in listOfMyISIN) {
            webSocketClient.send("{\"subscribe\":\"$isin\"}")
        }
    }

    private fun unsubscribeFromSocket() {
        for (isin in listOfMyISIN) {
            webSocketClient.send("{\"unsubscribe\":\"$isin\"}")
        }
    }

    private fun setUpStockPriceText(message: String?) {

        message?.let {

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter: JsonAdapter<Stocks> = moshi.adapter(Stocks::class.java)

            val stockRaw = adapter.fromJson(message)
            val stock = String.format("%.2f", stockRaw?.price?.toDouble())

            if (stockRaw != null && listOfMyISIN.indexOf(stockRaw.isin) != -1) {
                stockList.set(listOfMyISIN.indexOf(stockRaw.isin), stock + " \u20ac")
            }

            val observable = Observable.create<ArrayList<String>> {
                it.onNext(ArrayList(stockList))
            }

            observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ( {
                    rvAdapter = RVAdapter(ArrayList(stockList))
                    binding.rvList.adapter = rvAdapter
                },
                    { it.localizedMessage }
                )
        }
    }

    companion object {
        const val WEB_SOCKET_URL = "ws://159.89.15.214:8080/"
        const val TAG = "Trade Republic"

        //val stockNameToIsinMap<String,String> = mutableMapOf()
        //get listOfMyISIN as list of map's key
        val listOfMyISIN: ArrayList<String> = arrayListOf(
            "US0378331005",
            "DE000A1EWWW0",
            "NL0000235190",
            "DE0008404005",
            "US02079K3059",
            "US0231351067",
            "FR0000120628",
            "US0605051046",
            "DE000BAY0017",
            "US0846707026"
        )
    }
}

//            myCompositeDisposable?.add(obs
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(Schedulers.io())
//                    .doOnEach { Log.d(TAG, "SOMETHING IS HAPPENING OLOLOLO") }
//                    .subscribe {
//                        myStocksArrayList = ArrayList(it)
//                        rvAdapter = RVAdapter(myStocksArrayList!!)
//                        binding.securitiesList.adapter = rvAdapter
//                    }
//                )

//            if (stock != null) {
//                when(stock.isin) {
//                    "US0378331005" -> binding.textView1.text = "Apple _______________________ " + String.format("%.2f", stock.price?.toDouble())
//                    "DE000A1EWWW0" -> binding.textView2.text = "Adidas ______________________ " + String.format("%.2f", stock.price?.toDouble())
//                    "NL0000235190" -> binding.textView3.text = "Airbus ______________________ " + String.format("%.2f", stock.price?.toDouble())
//                    "DE0008404005" -> binding.textView4.text = "Allianz ______________________ " + String.format("%.2f", stock.price?.toDouble())
//                    "US02079K3059" -> binding.textView5.text = "Alphabet ____________________ " + String.format("%.2f", stock.price?.toDouble())
//                    "US0231351067" -> binding.textView6.text = "Amazon _____________________ " + String.format("%.2f", stock.price?.toDouble())
//                    "FR0000120628" -> binding.textView7.text = "Axa _________________________ " + String.format("%.2f", stock.price?.toDouble())
//                    "US0605051046" -> binding.textView8.text = "Bank of America _____________ " + String.format("%.2f", stock.price?.toDouble())
//                    "DE000BAY0017" -> binding.textView9.text = "Bayer _______________________ " + String.format("%.2f", stock.price?.toDouble())
//                    "US0846707026" -> binding.textView10.text = "Berkshire ___________________ " + String.format("%.2f", stock.price?.toDouble())
//                }
//            }