package com.example.movieswipe.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private var token: String? = null

    fun setToken(newToken: String) {
        token = newToken
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder: Request.Builder = chain.request().newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://localhost:3000/") // TODO: Replace with your backend URL if needed
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

