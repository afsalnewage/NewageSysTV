package com.dev.nastv.connection
import android.util.Log
import com.dev.nastv.uttils.AppConstant.BASE_URL
import com.dev.nastv.uttils.SessionUtils
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException
object SocketManager {
    private lateinit var mSocket: Socket


    fun initialize(token: String) {
        val options = IO.Options().apply {
           query = "token=$token"
           transports= arrayOf("websocket")

            //this.extraHeaders=mapOf("Authorization" to listOf(token)) //token
        }


        try {
            mSocket = IO.socket(BASE_URL , options)

        } catch (e: URISyntaxException) {
            Log.d("Excep23","${e.cause?.message}")
            throw RuntimeException(e)
        }
    }

    fun getSocket(): Socket {
        return mSocket
    }

    fun connect() {
        Log.d("Excep23","${SessionUtils.authToken}")
        mSocket.connect()
    }

    fun disconnect() {
        mSocket.disconnect()
    }
}
