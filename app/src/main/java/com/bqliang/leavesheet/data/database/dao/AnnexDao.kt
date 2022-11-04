package com.bqliang.leavesheet.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.bqliang.leavesheet.data.database.entity.Annex
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnexDao {
    @Insert(onConflict = REPLACE)
    suspend fun insetAnnex(annexes: List<Annex>)

    @Delete
    suspend fun deleteAnnex(annex: Annex)

    @Query("SELECT * FROM annex ORDER BY insert_time DESC")
    fun loadAllAnnexDesc(): Flow<List<Annex>>

    @Query("DELETE FROM annex WHERE id NOT in (SELECT id FROM annex ORDER BY insert_time DESC LIMIT 5)")
    suspend fun clean()
}