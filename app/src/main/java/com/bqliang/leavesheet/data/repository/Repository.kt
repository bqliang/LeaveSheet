package com.bqliang.leavesheet.data.repository

import com.bqliang.leavesheet.data.database.LeaveSheetDatabase
import com.bqliang.leavesheet.data.database.entity.Annex


object Repository {

    private val annexDao by lazy { LeaveSheetDatabase.getDatabase().annexDao() }

    suspend fun insertAnnexes(annexes: List<Annex>) {
        annexDao.insetAnnexes(annexes)
        annexDao.clean()
    }


    suspend fun deleteAnnex(annex: Annex) = annexDao.deleteAnnex(annex)
}