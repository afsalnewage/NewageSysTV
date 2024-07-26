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


class AuthorizationInterceptor  :Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var firstRequest = chain.request()

        if (!SessionUtils.authToken.isNullOrEmpty()) {
            firstRequest =
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + AppConstant.DEFAULT_AUTH)// SessionUtils.authToken!!)
                    .build()
        }
        var response = chain.proceed(firstRequest)
        Log.d("Respons12","respnos ${response.code}")
        if (response.code == 401 ) {//&& SessionUtils.hasSession()
            response.close()
            val map = HashMap<String, Any?>()
            map["token"] = SessionUtils.authToken!!
            map["refresh_token"] = SessionUtils.refreshToken!!
            val authRequest = firstRequest.newBuilder()
                .post(RequestBodyUtil.getRequestBodyMap(map))
                .url("$BASE_URL/auth/token").build()  // generate new token url
            response = chain.proceed(authRequest)

            if (response.isSuccessful) {
                try {
                    val auth =
                        JSONObject(response.body?.string()!!).getJSONObject("data").getString("token")
                    Log.d("Token23","Token $auth")
                    SessionUtils.saveAuthToken(auth)
                    response.close()
                    val secondRequest = firstRequest.newBuilder().removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $auth")
                        .method(firstRequest.method, firstRequest.body).build()
                    response = chain.proceed(secondRequest)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else if (response.code == 401) {
//                val intent = Intent(Intent.ACTION_MAIN)
//                intent.putExtra(INTENT_BROADCAST, AppConstants.BC_AUTH_LOGOUT)
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }

        return response
    }
}


//        override fun intercept(chain: Interceptor.Chain): Response {
//            var firstRequest = chain.request()
//
//
//            if (!SessionUtils.authToken.isNullOrEmpty()) {
//                firstRequest =
//                    chain.request().newBuilder()
//                        .addHeader("Authorization", "Bearer " + SessionUtils.authToken!!)
//                        .build()
//            }
//            var response = chain.proceed(firstRequest)
//            val authentication = response.header("Authentication")
//
//            if (authentication != null && authentication.equals("false", true)
//                && SessionUtils.refreshToken!!.isNotEmpty()
//            ) {
//
//                val builder = firstRequest.newBuilder()
//                    .addHeader("Authorization","Bearer " + SessionUtils.authToken!!)
//                    .addHeader("refresh-token", SessionUtils.refreshToken!!)
//                    .method(firstRequest.method, firstRequest.body)
//
//                val secondRequest = builder.build()
//                response.close()
//                response = chain.proceed(secondRequest)
//            } else if (response.code == 400 || response.code >= 500) {
//    //            //This for handling server issues
//    //            val localBroadcastManager = LocalBroadcastManager.getInstance(context)
//    //            val localIntent = Intent("custom-event-name").putExtra("server_down", true)
//    //            localBroadcastManager.sendBroadcast(localIntent)
//            }
//
//            val authToken = response.header("Authorization")
//            val refreshToken = response.header("refresh-token")
//
//
//            if (!authToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
//
//                SessionUtils.saveToken(authToken, refreshToken)
//            }
//            return response
//        }
//
//
//}