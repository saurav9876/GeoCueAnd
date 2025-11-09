package com.geocue.android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GeofenceStateDao {
    @Query("SELECT * FROM geofence_states WHERE geofenceId = :geofenceId")
    suspend fun getState(geofenceId: String): GeofenceStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(state: GeofenceStateEntity)

    @Query("UPDATE geofence_states SET hasDwelled = :hasDwelled WHERE geofenceId = :geofenceId")
    suspend fun updateDwellState(geofenceId: String, hasDwelled: Boolean)

    @Query("DELETE FROM geofence_states WHERE geofenceId = :geofenceId")
    suspend fun deleteState(geofenceId: String)

    @Query("DELETE FROM geofence_states")
    suspend fun clearAll()
}

