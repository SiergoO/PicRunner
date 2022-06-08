package com.picrunner.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.picrunner.data.BuildConfig.API_URL
import com.picrunner.data.BuildConfig.FLICKR_API_KEY
import com.pricrunner.data.rest.FlickrApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    private const val HTTP_CONNECT_TIMEOUT_MS = 20 * 1000
    private const val HTTP_READ_TIMEOUT_MS = 20 * 1000

    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    fun provideAuthorizationInterceptor(): Interceptor =
        Interceptor { chain ->
            val originalRequest: Request = chain.request()
            val url = originalRequest.url
                .newBuilder()
                .addQueryParameter("format", "json")
                .addQueryParameter("nojsoncallback", "?")
                .addQueryParameter("api_key", FLICKR_API_KEY)
                .build()
            val newRequest = originalRequest
                .newBuilder()
                .url(url)
                .build()
            chain.proceed(newRequest)
        }

    @Provides
    fun provideOkHttp(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        authorizationInterceptor: Interceptor,
    ): OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(HTTP_CONNECT_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        readTimeout(HTTP_READ_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        addInterceptor(httpLoggingInterceptor)
        addInterceptor(authorizationInterceptor)
    }.build()

    @Provides
    fun provideJson(): Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    @ExperimentalSerializationApi
    @Provides
    fun provideRetrofit(json: Json, okHttpClient: OkHttpClient): FlickrApi =
        createRetrofit(json, okHttpClient).create(FlickrApi::class.java)

    @ExperimentalSerializationApi
    private fun createRetrofit(json: Json, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(API_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

}