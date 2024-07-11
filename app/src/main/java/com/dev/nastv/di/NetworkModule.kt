package com.dev.nastv.di

import android.content.Context
import com.dev.nastv.apis.ApiService
//import com.dev.nastv.db.AppDatabase
import com.dev.nastv.network.AuthorizationInterceptor
import com.dev.nastv.network.FlowCallAdapterFactory
import com.dev.nastv.uttils.AppConstant.BASE_URL
import com.dev.nastv.uttils.ConnectivityObserver
import com.dev.nastv.uttils.NetworkConnectivityObserver
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
//import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {


    @Provides
    @Singleton
    fun provideJson() = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        networkInterceptor: AuthorizationInterceptor,
        @ApplicationContext context: Context
    ): OkHttpClient =
        OkHttpClient.Builder().run {
//            if (BuildConfig.DEBUG) {
//                val logging = HttpLoggingInterceptor()
//                logging.level = HttpLoggingInterceptor.Level.BODY
//                addInterceptor(logging)
//            }
            connectTimeout(1, TimeUnit.MINUTES)
            readTimeout(1, TimeUnit.MINUTES)
            writeTimeout(1, TimeUnit.MINUTES)
            callTimeout(1, TimeUnit.MINUTES)
            addInterceptor(networkInterceptor)
            build()
        }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(
                //GsonConverterFactory.create()
                json.asConverterFactory("application/json".toMediaType())

            )
           .addCallAdapterFactory( FlowCallAdapterFactory())
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideAppApi(
        retrofit: Retrofit
    ): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideConnectivityObserver(
        @ApplicationContext context: Context
    ): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

    @Singleton
    @Provides
    fun provideAuthorizationInterceptor(
       // app: Application,
        //dataStoreManager: DataStoreManager,
      //  @ApplicationScope externalScope: CoroutineScope
    ) = AuthorizationInterceptor()

    @Singleton
    @Provides
    fun getBaseUrl(): String {
        return BASE_URL
    }


//    @Provides
//    @Singleton
//    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
//        return Room.databaseBuilder(
//            context.applicationContext,
//            AppDatabase::class.java,
//            "app_database"
//        ).build()
//    }

//    @Provides
//    @Singleton
//    fun provideDownloadDao(database: AppDatabase): DownloadDao {
//        return database.downloadDao()
//    }


}