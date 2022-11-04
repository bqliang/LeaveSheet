package com.bqliang.leavesheet.data.repository

import com.bqliang.leavesheet.data.database.MyDatabase
import com.bqliang.leavesheet.data.database.entity.Annex


object Repository {

    private val annexDao by lazy { MyDatabase.getDatabase().annexDao() }

    suspend fun updateAnnexList(annexList: List<Annex>) {
        annexDao.insetAnnex(annexList)
        annexDao.clean()
    }
}