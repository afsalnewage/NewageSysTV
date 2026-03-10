package com.dev.nastv.network

import android.content.Context
import android.content.Intent
import android.util.Log
import com.dev.nastv.uttils.AppConstant.BASE_URL
import com.dev.nastv.uttils.AppConstant.TOKEN_EXPIRE
import com.dev.nastv.uttils.RequestBodyUtil
import com.dev.nastv.uttils.SessionUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

class AuthorizationIntercepteor1(val context: Context) : Interceptor {
    companion object {
        private val AUTHENTICATOR_LOCK = Any()
    }


    override fun intercept(chain: Interceptor.Chain): Response {
        var firstRequest = chain.request()

        // 1. Add the current token to the initial request
        if (!SessionUtils.authToken.isNullOrEmpty()) {
            firstRequest = firstRequest.newBuilder()
                .header("Authorization", "Bearer ${SessionUtils.authToken!!}")
                .build()
        }

        // 2. Proceed with the initial request
        var response = chain.proceed(firstRequest)
        Log.d("Interceptor", "Initial Request: ${response.code}")


        // 3. Check for 401 Unauthorized
        if (response.code == 401) {
            // Close the response body immediately
            response.close()

            // 4. Enter the critical section
            synchronized(AUTHENTICATOR_LOCK) {

                // CRITICAL STEP: Check if the token was refreshed by another thread while waiting for the lock
                val currentTokenInRequest = firstRequest.header("Authorization")?.removePrefix("Bearer ")

                if (SessionUtils.authToken != currentTokenInRequest) {
                    Log.d("Interceptor", "Token already refreshed. Retrying original request.")
                    // Token was refreshed; retry the original request with the NEW saved token
                    val newRequest = firstRequest.newBuilder()
                        .header("Authorization", "Bearer ${SessionUtils.authToken!!}")
                        .build()
                    return chain.proceed(newRequest)
                }

                // --- Token Refresh Process (Only runs if the token is still stale) ---

                // Prepare the body for the refresh request (using both tokens)
                val map = HashMap<String, Any?>()
                map["token"] = SessionUtils.authToken!!
                map["refresh_token"] = SessionUtils.refreshToken!!

                // Build the refresh request. NOTE: Ensure it does NOT have the Authorization header
                val authRequest = Request.Builder()
                    .url("$BASE_URL/auth/token")
                    .post(RequestBodyUtil.getRequestBodyMap(map))
                    .build()

                Log.d("Interceptor", "Attempting token refresh...")

                // Execute the refresh request
                val authResponse = chain.proceed(authRequest)

                if (authResponse.isSuccessful) {
                    try {
                        // Extracting and saving the new token
                        val authResponseBody = authResponse.body?.string()
                        val newAuthToken =
                            JSONObject(authResponseBody!!).getJSONObject("data").getString("token")

                        Log.d("Interceptor", "Token refreshed successfully.")

                        SessionUtils.saveAuthToken(newAuthToken)

                        // Send broadcast to update other active API clients (optional)
                        context.sendBroadcast(Intent(TOKEN_EXPIRE).putExtra("new_token", newAuthToken))

                        authResponse.close()

                        // 5. Create and proceed with the second request (retry original)
                        val secondRequest = firstRequest.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer $newAuthToken")
                            .build()

                        // Proceed with the retried request and return its response
                        return chain.proceed(secondRequest)

                    } catch (e: Exception) {
                        Log.e("Interceptor", "Error parsing refresh token response or retrying.", e)
                        authResponse.close()
                        // If parsing or saving fails, treat it as a failed login
                       // context.sendBroadcast(Intent(FORCE_LOGOUT_ACTION))
                        // Returning the original 401 response is usually best here.
                    }

                } else {
                    // 6. Handle failed refresh (Refresh token is invalid or expired)
                    Log.e("Interceptor", "Refresh token request failed with code: ${authResponse.code}. Forcing logout.")
                    authResponse.close()
                    // Send broadcast to trigger immediate logout/redirect to login screen
                   // context.sendBroadcast(Intent(FORCE_LOGOUT_ACTION))

                    // Return the original 401 response to the original caller
                }
            } // End of synchronized block
        }

        // 7. If not a 401, or if refresh failed, return the final response
        return response
    }
}