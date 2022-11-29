package com.bqliang.leavesheet.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bqliang.leavesheet.MyApp
import com.bqliang.leavesheet.data.database.dao.AnnexDao
import com.bqliang.leavesheet.data.database.entity.Annex

@Database(version = 1, entities = [Annex::class])
abstract class LeaveSheetDatabase : RoomDatabase() {

    abstract fun annexDao(): AnnexDao

    companion object {
        @Volatile
        private var INSTANCE: LeaveSheetDatabase? = null

        fun getDatabase(): LeaveSheetDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    MyApp.context,
                    LeaveSheetDatabase::class.java,
                    "leave_sheet.db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
    }
}