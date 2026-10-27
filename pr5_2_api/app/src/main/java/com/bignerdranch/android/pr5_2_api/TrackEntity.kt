package com.bignerdranch.android.pr5_2_api

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

// Описание таблицы для песен
@Entity(tableName = "playlist_table")
data class TrackEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "track_name") var trackName: String,
    @ColumnInfo(name = "artist_name") var artistName: String,
    @ColumnInfo(name = "duration") var duration: String,
    @ColumnInfo(name = "spotify_url") var spotifyUrl: String = ""
)

@Dao
interface TrackDao {
    @Query("SELECT * FROM playlist_table ORDER BY track_name ASC")
    fun getAllTracks(): LiveData<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(track: TrackEntity)

    @Update
    fun updateTrack(track: TrackEntity)

    @Delete
    fun deleteTrack(track: TrackEntity)
}

@Database(entities = [TrackEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_playlist_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}