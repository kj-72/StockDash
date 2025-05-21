package com.example.stockdash.di

import com.example.stockdash.data.network.AlphaVantageApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Install this module in SingletonComponent (application scope) It'll live as long as the application does
object NetworkModule {

    private const val BASE_URL = "https://www.alphavantage.co/"

    @Provides
    @Singleton // Tells Hilt this should be a singleton (one instance for the app)
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build() // Moshi to convert JSON to Kotlin objects (KotlinJsonAdapterFactory)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder() // underlying HTTP engine used by Retrofit to actually send and receive network requests.
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (true) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE // Debug (true)
            })
//            .cache(Cache(cacheDir, cacheSize)) // Cache for offline use tho we are using ROOM for more control
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // Use Moshi to convert JSON to Kotlin objects (DTO Format)
            .build()
    }

    @Provides
    @Singleton
    fun provideAlphaVantageApiService(retrofit: Retrofit): AlphaVantageApiService {
        return retrofit.create(AlphaVantageApiService::class.java) // Retrofit will take AlphaVantageApiService interface and generate its implementation
    }
}