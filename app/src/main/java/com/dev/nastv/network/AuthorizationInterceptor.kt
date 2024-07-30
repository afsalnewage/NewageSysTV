package com.dev.nastv.network

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dev.nastv.di.NetworkModule
import com.dev.nastv.uttils.AppConstant
import com.dev.nastv.uttils.AppConstant.BASE_URL
import com.dev.nastv.uttils.RequestBodyUtil
import com.dev.nastv.uttils.SessionUtils
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.util.HashMap

class AuthorizationInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var firstRequest = chain.request()

        // Adding the Authorization header if the token is not null or empty
        if (!SessionUtils.authToken.isNullOrEmpty()) {
            firstRequest = firstRequest.newBuilder()
                .addHeader("Authorization", "Bearer " + SessionUtils.authToken!!).build()
        }

        // Proceeding with the first request
        var response = chain.proceed(firstRequest)
        Log.d("Response", "Response code: ${response.code}")

        // If the response code is 401 (Unauthorized)
        if (response.code == 401) {
            response.close()  // Close the response

            // Creating a map for the token and refresh token
            val map = HashMap<String, Any?>()
            map["token"] = SessionUtils.authToken!!
            map["refresh_token"] = SessionUtils.refreshToken!!

            // Creating the request to get a new token
            val authRequest = firstRequest.newBuilder().post(RequestBodyUtil.getRequestBodyMap(map))
                .url("$BASE_URL/auth/token").build()

            // Proceeding with the auth request
            val authResponse = chain.proceed(authRequest)

            if (authResponse.isSuccessful) {
                try {
                    // Extracting the new token from the response
                    val authResponseBody = authResponse.body?.string()
                    val auth =
                        JSONObject(authResponseBody!!).getJSONObject("data").getString("token")
                    Log.d("Response", "New Token: $auth")

                    // Saving the new token
                    SessionUtils.saveAuthToken(auth)
                    authResponse.close()

                    // Creating a new request with the new token
                    val secondRequest = firstRequest.newBuilder().removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $auth").build()

                    // Proceeding with the second request
                    response = chain.proceed(secondRequest)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else if (authResponse.code == 401) {
                // Handle the case where the refresh token also fails
                // You can log out the user or take any other appropriate action
//                val intent = Intent(Intent.ACTION_MAIN)
//                intent.putExtra(INTENT_BROADCAST, AppConstants.BC_AUTH_LOGOUT)
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }

        return response
    }
}

