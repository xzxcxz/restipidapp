package com.diva.restofinder.di

import android.content.Context
import androidx.room.Room
import com.diva.restofinder.db.FavoriteRestaurantDao
import com.diva.restofinder.db.RestipidDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRestipidDatabase(
        @ApplicationContext context: Context
    ): RestipidDatabase {
        return Room.databaseBuilder(
            context,
            RestipidDatabase::class.java,
            "restipid_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavoriteRestaurantDao(
        db: RestipidDatabase
    ): FavoriteRestaurantDao = db.favoriteRestaurantDao()

}