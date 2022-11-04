package com.bqliang.leavesheet.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bqliang.leavesheet.LeaveSheet
import com.bqliang.leavesheet.MyApp
import com.bqliang.leavesheet.data.database.MyDatabase
import com.bqliang.leavesheet.data.database.dao.AnnexDao
import com.bqliang.leavesheet.data.database.entity.Annex
import com.bqliang.leavesheet.data.datastore.SettingsDataStore
import com.bqliang.leavesheet.data.datastore.leaveSheetDataStore
import com.bqliang.leavesheet.data.repository.Repository
import kotlinx.coroutines.launch

class LeaveSheetViewModel : ViewModel() {

    private val annexDao: AnnexDao by lazy { MyDatabase.getDatabase().annexDao() }

    val annexes: LiveData<List<Annex>> = annexDao.loadAllAnnexDesc().asLiveData()
    val leaveSheet: LiveData<LeaveSheet> = MyApp.context.leaveSheetDataStore.data.asLiveData()
    val facultyAuditVisible = SettingsDataStore.facultyAuditVisible.asLiveData()

    fun openWeWork() {
        val intent = MyApp.context.packageManager.getLaunchIntentForPackage("com.tencent.wework")
        MyApp.context.startActivity(intent)
    }


    fun saveAnnex(names: List<String>) {
        viewModelScope.launch {
            val annexes = names.map { Annex(it) }
            Repository.updateAnnexList(annexes)
        }
    }


    fun deleteAnnex(annex: Annex) = viewModelScope.launch {
        annexDao.deleteAnnex(annex)
    }
}