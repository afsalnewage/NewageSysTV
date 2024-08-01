package com.dev.nastv.network

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dev.nastv.connection.SocketManager
import com.dev.nastv.di.NetworkModule
import com.dev.nastv.uttils.AppConstant
import com.dev.nastv.uttils.AppConstant.BASE_URL
import com.dev.nastv.uttils.AppConstant.TOKEN_EXPIRE
import com.dev.nastv.uttils.RequestBodyUtil
import com.dev.nastv.uttils.SessionUtils
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.util.HashMap

class AuthorizationInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var firstRequest = chain.request()

        if (!SessionUtils.authToken.isNullOrEmpty()) {
            firstRequest = firstRequest.newBuilder()
                .addHeader("Authorization", "Bearer " + SessionUtils.authToken!!).build()
        }


        var response = chain.proceed(firstRequest)
        Log.d("Response", "Response code: ${response.code}")


        if (response.code == 401) {
            response.close()


            val map = HashMap<String, Any?>()
            map["token"] = SessionUtils.authToken!!
            map["refresh_token"] = SessionUtils.refreshToken!!

            val authRequest = firstRequest.newBuilder().removeHeader("Authorization")
               // .addHeader("User-Agent", "jkkkkkkkkkkkloitttt")
                .post(RequestBodyUtil.getRequestBodyMap(map))
                .url("$BASE_URL/auth/token")

                .build()
//            Log.d("Response", "authRequest ${authRequest}")
//            Log.d("Response", "authRequest body ${authRequest.body.toString()}")
//            Log.d("Response", "authRequest headers ${authRequest.headers}")


            val authResponse = chain.proceed(authRequest)
//            Log.d("Response", "authRespons ${authResponse.code}")
//            Log.d("Response", "authRespons ${authResponse.message}")
            if (authResponse.isSuccessful) {
                try {
                    // Extracting the new token from the response
                    val authResponseBody = authResponse.body?.string()
                    val auth =
                        JSONObject(authResponseBody!!).getJSONObject("data").getString("token")
                    Log.d("Response122", "New Token: $auth")

                    val intent = Intent(TOKEN_EXPIRE)
                    intent.putExtra("new_token", auth)
                    context.sendBroadcast(intent)
                    // Saving the new token
                    SessionUtils.saveAuthToken(auth)
                    authResponse.close()

                    // Creating a new request with the new token
                    val secondRequest = firstRequest.newBuilder().removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $auth").build()
//


                    // Proceeding with the second request
                    response = chain.proceed(secondRequest)

                } catch (e: Exception) {
                    e.printStackTrace()
                 }

            } else if (authResponse.code == 401) {

//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }

        return response
    }
}

