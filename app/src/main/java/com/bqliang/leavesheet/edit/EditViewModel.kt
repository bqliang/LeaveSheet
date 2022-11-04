package com.bqliang.leavesheet.edit

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bqliang.leavesheet.MyApp
import com.bqliang.leavesheet.data.datastore.SettingsDataStore
import com.bqliang.leavesheet.data.datastore.leaveSheetDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditViewModel : ViewModel() {

    val faculty = MutableLiveData("")
    val major = MutableLiveData("")
    val className = MutableLiveData("")
    val applicant = MutableLiveData("")
    val stuId = MutableLiveData("")
    val phone = MutableLiveData("")

    val type = MutableLiveData("")
    val durationInDays = MutableLiveData(0f)
    val reason = MutableLiveData("")
    val counselor = MutableLiveData("")
    val counselorAuditOpinion = MutableLiveData("")
    val facultyAuditor = MutableLiveData("")
    val facultyAuditOpinion = MutableLiveData("")

    val applicationDate = MutableLiveData(0L)
    val startTime = MutableLiveData(0L)
    val endTime = MutableLiveData(0L)
    val counselorAuditTime = MutableLiveData(0L)
    val facultyAuditTime = MutableLiveData(0L)

    val facultyAuditVisible = SettingsDataStore.facultyAuditVisible.asLiveData()

    init {
        viewModelScope.launch {
            MyApp.context.leaveSheetDataStore.data.collect {
                faculty.value = it.faculty
                major.value = it.major
                className.value = it.className
                applicant.value = it.applicant
                stuId.value = it.stuId
                phone.value = it.phone

                type.value = it.type
                durationInDays.value = it.durationInDays
                reason.value = it.reason
                counselor.value = it.counselor
                counselorAuditOpinion.value = it.counselorAuditOpinion
                facultyAuditor.value = it.facultyAuditor
                facultyAuditOpinion.value = it.facultyAuditOpinion
            }
        }

        viewModelScope.launch {
            MyApp.context.leaveSheetDataStore.data.first().let {
                applicationDate.value = it.applicationDate
                startTime.value = it.startTime
                endTime.value = it.endTime
                counselorAuditTime.value = it.counselorAuditTime
                facultyAuditTime.value = it.facultyAuditTime
            }
        }
    }


    fun update() = viewModelScope.launch {
        MyApp.context.leaveSheetDataStore.updateData {
            it.toBuilder()
                /* 个人信息 */
                .setFaculty(faculty.value)
                .setMajor(major.value)
                .setClassName(className.value)
                .setApplicant(applicant.value)
                .setStuId(stuId.value)
                .setPhone(phone.value)
                /* 假条信息 */
                .setType(type.value)
                .setDurationInDays(durationInDays.value!!)
                .setReason(reason.value)
                .setCounselor(counselor.value)
                .setCounselorAuditOpinion(counselorAuditOpinion.value)
                .setFacultyAuditor(facultyAuditor.value)
                .setFacultyAuditOpinion(facultyAuditOpinion.value)
                .setApplicationDate(applicationDate.value!!)
                .setStartTime(startTime.value!!)
                .setEndTime(endTime.value!!)
                .setCounselorAuditTime(counselorAuditTime.value!!)
                .setFacultyAuditTime(facultyAuditTime.value!!)
                .build()
        }
        Toast.makeText(MyApp.context, "保存成功", Toast.LENGTH_SHORT).show()
    }
}