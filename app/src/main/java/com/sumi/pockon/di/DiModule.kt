package com.sumi.pockon.di

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat.getString
import androidx.room.Room
import com.sumi.pockon.data.local.brand.BrandDatabase
import com.sumi.pockon.data.local.gift.GiftDatabase
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.R
import com.sumi.pockon.data.remote.brand.KaKaoSearchAPI
import com.sumi.pockon.data.local.gift.GiftLocalDataSource
import com.sumi.pockon.data.local.brand.BrandDao
import com.sumi.pockon.data.local.brand.BrandLocalDataSource
import com.sumi.pockon.data.local.gift.GiftDao
import com.sumi.pockon.data.remote.gift.GiftDataRemoteSource
import com.sumi.pockon.data.remote.gift.GiftPhotoRemoteDataSource
import com.sumi.pockon.data.remote.login.LoginDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sumi.pockon.data.local.alarm.AlarmDataSource
import com.sumi.pockon.data.local.preference.PreferenceLocalDataSource
import com.sumi.pockon.data.repository.PreferenceRepository
import com.sumi.pockon.data.repository.AlarmRepository
import com.sumi.pockon.util.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class DiModule {
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideLoginDataSource(auth: FirebaseAuth, @ApplicationContext context: Context): LoginDataSource {
        return LoginDataSource(auth, context)
    }

    @Singleton
    @Provides
    fun provideGiftRepository(
        giftDataRemoteSource: GiftDataRemoteSource,
        giftPhotoRemoteDataSource: GiftPhotoRemoteDataSource,
        giftLocalDataSource: GiftLocalDataSource,
        @ApplicationContext context: Context
    ): GiftRepository {
        return GiftRepository(giftDataRemoteSource, giftPhotoRemoteDataSource, giftLocalDataSource, context)
    }

    @Singleton
    @Provides
    fun provideGiftDataRemoteSource(firestore: FirebaseFirestore): GiftDataRemoteSource {
        return GiftDataRemoteSource(firestore)
    }

    @Singleton
    @Provides
    fun provideKakaoRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://dapi.kakao.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Singleton
    @Provides
    fun provideKakaoSearchApi(retrofit: Retrofit): KaKaoSearchAPI =
        retrofit.create(KaKaoSearchAPI::class.java)

    @Provides
    fun provideBrandLocalDataSource(brandsDao: BrandDao): BrandLocalDataSource {
        return BrandLocalDataSource(brandsDao)
    }

    @Provides
    fun provideGiftLocalDataSource(giftDao: GiftDao): GiftLocalDataSource {
        return GiftLocalDataSource(giftDao)
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestorage(): StorageReference {
        return FirebaseStorage.getInstance().reference
    }

    @Singleton
    @Provides
    fun provideGiftPhotoRemoteDataSource(storageRef: StorageReference): GiftPhotoRemoteDataSource {
        return GiftPhotoRemoteDataSource(storageRef)
    }

    @Singleton
    @Provides
    fun provideSharedPref(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(
            getString(context, R.string.preference_file_key), Context.MODE_PRIVATE
        )
    }

    @Singleton
    @Provides
    fun provideBrandDatabase(
        @ApplicationContext context: Context
    ): BrandDatabase = Room.databaseBuilder(context, BrandDatabase::class.java, "brand.db").build()

    @Singleton
    @Provides
    fun provideBrandDao(brandDatabase: BrandDatabase): BrandDao = brandDatabase.brandDao()

    @Singleton
    @Provides
    fun provideGiftDatabase(
        @ApplicationContext context: Context
    ): GiftDatabase = Room.databaseBuilder(context, GiftDatabase::class.java, "gift.db").build()

    @Singleton
    @Provides
    fun provideGiftDao(giftDatabase: GiftDatabase): GiftDao = giftDatabase.giftDao()

    @Singleton
    @Provides
    fun provideAlarmRepository(alarmDataSource: AlarmDataSource): AlarmRepository = AlarmRepository(alarmDataSource)

    @Singleton
    @Provides
    fun provideAlarmDataSource(@ApplicationContext context: Context): AlarmDataSource = AlarmDataSource(context)

    @Singleton
    @Provides
    fun providePreferenceRepository(preferenceLocalDataSource: PreferenceLocalDataSource): PreferenceRepository {
        return PreferenceRepository(preferenceLocalDataSource)
    }

    @Singleton
    @Provides
    fun providePreferenceLocalDataSource(sharedPreferences: SharedPreferences): PreferenceLocalDataSource {
        return PreferenceLocalDataSource(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }
}
