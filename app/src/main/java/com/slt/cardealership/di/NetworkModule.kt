package com.slt.cardealership.di

import com.google.gson.GsonBuilder
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.data.remote.network.AuthInterceptor
import com.slt.cardealership.data.remote.network.TokenAuthenticator
import com.slt.cardealership.domain.repo.AuthRepository
import dagger.Module

import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlin.math.log

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://backend-api-stg.sba.net/"

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        sessionManager: SessionManager,
        authRepository: AuthRepository
    ): TokenAuthenticator {
        return TokenAuthenticator(sessionManager, authRepository)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

}
