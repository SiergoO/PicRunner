package com.picrunner.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.picrunner.BuildConfig.API_URL
import com.pricrunner.data.rest.FlickrApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    private const val HTTP_CONNECT_TIMEOUT_MS = 20 * 1000
    private const val HTTP_READ_TIMEOUT_MS = 20 * 1000

    @Provides
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(HTTP_CONNECT_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        readTimeout(HTTP_READ_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
    }.build()

    @Provides
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @ExperimentalSerializationApi
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): FlickrApi =
        createRetrofit(okHttpClient).create(FlickrApi::class.java)

    @ExperimentalSerializationApi
    private fun createRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
}