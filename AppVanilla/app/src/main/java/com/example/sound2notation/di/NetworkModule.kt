package com.example.sound2notation.di

import android.content.Context
import com.example.sound2notation.data.network.ApiService
import com.example.sound2notation.data.network.SharedPreferencesCookieJar
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    const val CONNTYPE = "http"
    const val IP = "192.168.43.185"
    const val PORT = "5000"

//    private const val BASE_URL = "http://192.168.0.100:5000/"
//    private const val BASE_URL = "http://192.168.43.185:5000/"
    private const val BASE_URL = CONNTYPE + "://" + IP + ":" + PORT + "/"


    private lateinit var cookieJar: SharedPreferencesCookieJar

    fun initialize(context: Context) {
        if (!this::cookieJar.isInitialized) {
            cookieJar = SharedPreferencesCookieJar(context)

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .cookieJar(cookieJar)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
    }

    private lateinit var retrofit: Retrofit
    lateinit var apiService: ApiService

    fun clearCookies() {
        if (this::cookieJar.isInitialized) {
            cookieJar.clearAllCookies()
        }
    }
}