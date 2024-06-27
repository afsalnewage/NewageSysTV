package com.dev.nastv.connection
import com.dev.nastv.uttils.AppConstant.BASE_URL
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException
object SocketManager {
    private lateinit var mSocket: Socket
    private const val SERVER_URL = "http://yourserver.com"  // Replace with your server URL

    fun initialize(token: String) {
        val options = IO.Options().apply {
            query = "token=$token" // Pass token as a query parameter
        }

        try {
            mSocket = IO.socket(BASE_URL, options)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    fun getSocket(): Socket {
        return mSocket
    }

    fun connect() {
        mSocket.connect()
    }

    fun disconnect() {
        mSocket.disconnect()
    }
}
