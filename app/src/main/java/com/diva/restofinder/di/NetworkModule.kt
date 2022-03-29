package com.diva.restofinder.di

import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.diva.restofinder.networking.ApiEndpoint
import com.diva.restofinder.networking.ZomatoAPI
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofitInstance(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiEndpoint.BASEURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideZomatoAPI(retrofit: Retrofit): ZomatoAPI =
        retrofit.create(ZomatoAPI::class.java)

}