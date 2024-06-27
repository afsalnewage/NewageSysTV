package com.dev.nastv.uttils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Singleton

@Singleton
class RequestBodyUtil {
    companion object {
        fun getRequestBodyMap(reqMap: HashMap<String, Any?>): RequestBody {
            val reqJson = JSONObject()
            for ((key, value) in reqMap) {

                try {
                    if (value !=null) {
                        when (value) {
                            is Int -> reqJson.put(key, Integer.parseInt(value.toString()))
                            is Double -> reqJson.put(key, java.lang.Double.parseDouble(value.toString()))
                            is Boolean -> reqJson.put(key, java.lang.Boolean.parseBoolean(value.toString()))
                            is Long -> reqJson.put(key, java.lang.Long.parseLong(value.toString()))
                            is Float -> reqJson.put(key, java.lang.Float.parseFloat(value.toString()).toDouble())
                            is ArrayList<*>-> reqJson.put(key, JSONArray(value))
                            is JSONArray -> reqJson.put(key, value)
                            else -> reqJson.putOpt(key, value)
                        }
                    }else{
                        reqJson.put(key, JSONObject.NULL)
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            //return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), reqJson.toString())
            //return RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), reqJson.toString())
            return reqJson.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
    }
}