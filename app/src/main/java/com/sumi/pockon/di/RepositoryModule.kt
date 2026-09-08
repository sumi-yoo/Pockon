package com.sumi.pockon.di

import com.sumi.pockon.data.repository.GiftRepositoryImpl
import com.sumi.pockon.data.repository.AlarmRepositoryImpl
import com.sumi.pockon.data.repository.LoginRepository
import com.sumi.pockon.data.repository.BrandRepositoryImpl
import com.sumi.pockon.domain.repository.BrandRepository
import com.sumi.pockon.domain.repository.GiftRepository
import com.sumi.pockon.domain.repository.AuthRepository
import com.sumi.pockon.domain.repository.AlarmRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAlarmRepository(alarmRepositoryImpl: AlarmRepositoryImpl): AlarmRepository

    @Binds
    abstract fun bindAuthRepository(loginRepository: LoginRepository): AuthRepository

    @Binds
    abstract fun bindBrandRepository(
        brandRepositoryImpl: BrandRepositoryImpl
    ): BrandRepository

    @Binds
    abstract fun bindGiftRepository(
        giftRepositoryImpl: GiftRepositoryImpl
    ): GiftRepository
}
