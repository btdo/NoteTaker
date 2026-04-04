package com.noteaker.sample.di

import com.noteaker.sample.BuildConfig
import com.noteaker.sample.data.network.FakeSyncApi
import com.noteaker.sample.data.network.ImageApi
import com.noteaker.sample.data.network.SyncApi
import com.noteaker.sample.data.network.ZenQuotesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val ZENQUOTES_BASE_URL = "https://zenquotes.io"

    private const val LOREM_BASE_URL = "https://picsum.photos"

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            )
        }

        return builder.build()
    }

    @Provides
    fun provideZenQuotesApi(okHttpClient: OkHttpClient): ZenQuotesApi {
        return Retrofit.Builder()
            .baseUrl(ZENQUOTES_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ZenQuotesApi::class.java)
    }

    @Provides
    fun provideImageApi(okHttpClient: OkHttpClient): ImageApi {
        return Retrofit.Builder()
            .baseUrl(LOREM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ImageApi::class.java)
    }

    @Provides
    fun provideSyncApi(okHttpClient: OkHttpClient): SyncApi {
        return FakeSyncApi()
    }
}
