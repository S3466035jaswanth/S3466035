package uk.ac.tees.mad.bloodconnect.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BloodRequestEntity::class], version = 1, exportSchema = false)
abstract class BloodRequestDatabase : RoomDatabase() {
    abstract fun bloodRequestDao(): BloodRequestDao
    companion object {
        @Volatile
        private var INSTANCE: BloodRequestDatabase? = null
        fun getDatabase(context: Context): BloodRequestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BloodRequestDatabase::class.java,
                    "blood_request_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}