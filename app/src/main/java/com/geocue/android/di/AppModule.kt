package com.geocue.android.di

import android.app.Application
import androidx.room.Room
import com.geocue.android.data.GeofenceRepository
import com.geocue.android.data.local.GeoCueDatabase
import com.geocue.android.location.AndroidLocationClient
import com.geocue.android.location.GeofenceController
import com.geocue.android.notifications.GeofenceNotificationManager
import com.geocue.android.notifications.NotificationChannels
import com.geocue.android.permissions.PermissionChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): GeoCueDatabase =
        Room.databaseBuilder(app, GeoCueDatabase::class.java, "geocue.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideGeofenceRepository(database: GeoCueDatabase): GeofenceRepository =
        GeofenceRepository(database.geofenceDao())

    @Provides
    @Singleton
    fun provideLocationClient(app: Application): AndroidLocationClient = AndroidLocationClient(app)

    @Provides
    @Singleton
    fun provideGeofenceController(app: Application): GeofenceController = GeofenceController(app)

    @Provides
    @Singleton
    fun providePermissionChecker(app: Application): PermissionChecker = PermissionChecker(app)

    @Provides
    @Singleton
    fun provideNotificationManager(app: Application, database: GeoCueDatabase): GeofenceNotificationManager =
        GeofenceNotificationManager(app, NotificationChannels(app), database)
}
