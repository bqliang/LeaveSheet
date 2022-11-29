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

    companion object {
        const val ANNEXES_MAX_LIMIT = 6
    }

    @Insert(onConflict = REPLACE)
    suspend fun insetAnnexes(annexes: List<Annex>)

    @Delete
    suspend fun deleteAnnex(annex: Annex)

    @Query("SELECT * FROM annex ORDER BY insert_time DESC")
    fun loadAllAnnexDesc(): Flow<List<Annex>>

    @Query("DELETE FROM annex WHERE id NOT in (SELECT id FROM annex ORDER BY insert_time DESC LIMIT $ANNEXES_MAX_LIMIT)")
    suspend fun clean()

    @Query("SELECT COUNT(*) FROM annex")
    suspend fun getAnnexCount(): Int
}