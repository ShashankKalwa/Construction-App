package com.example.contruction_management_app.core.network

import android.content.Context
import com.example.contruction_management_app.BuildConfig
import com.example.contruction_management_app.core.auth.SessionDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val sessionDataStore: SessionDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionDataStore.authTokenFlow.firstOrNull() }
        val builder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            builder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}

object NetworkClient {
    @Volatile
    private var apiService: CmsApiService? = null

    fun getApiService(context: Context): CmsApiService {
        return apiService ?: synchronized(this) {
            apiService ?: buildRetrofit(context).create(CmsApiService::class.java).also {
                apiService = it
            }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val sessionDataStore = SessionDataStore(context.applicationContext)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionDataStore))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(7, TimeUnit.SECONDS)
            .writeTimeout(7, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
