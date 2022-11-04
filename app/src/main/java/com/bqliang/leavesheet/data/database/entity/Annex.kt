package com.bqliang.leavesheet.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "annex", indices = [Index("file_name", unique = true)])
data class Annex(
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "insert_time") val insertTime: Long = System.currentTimeMillis()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}