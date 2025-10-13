package com.slt.cardealership.di

import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.data.remote.auth.AuthRepositoryImpl
import com.slt.cardealership.data.repo.DealerRepositoryImpl
import com.slt.cardealership.data.repo.PostRepositoryImpl
//import com.slt.cardealership.domain.repo.ArticleRepository
import com.slt.cardealership.domain.repo.AuthRepository
import com.slt.cardealership.domain.repo.DealerRepository
import com.slt.cardealership.domain.repo.PostRepository
import com.slt.cardealership.domain.usecase.SignInUseCase
import com.slt.cardealership.domain.usecase.SilentLoginUseCase
import com.slt.cardealership.domain.usecase.SignOutUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindDealerRepository(impl: DealerRepositoryImpl): DealerRepository

    companion object {
        @Provides
        @Singleton
        fun provideSignInUseCase(repo: AuthRepository): SignInUseCase = SignInUseCase(repo)

        @Provides
        @Singleton
        fun provideSilentLoginUseCase(repo: AuthRepository,
                                      sessionManager: SessionManager): SilentLoginUseCase = SilentLoginUseCase(repo,sessionManager)

        @Provides
        @Singleton
        fun provideSignOutUseCase(repo: AuthRepository): SignOutUseCase = SignOutUseCase(repo)
    }


}
