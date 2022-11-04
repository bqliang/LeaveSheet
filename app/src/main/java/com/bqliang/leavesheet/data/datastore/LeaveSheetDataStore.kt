package com.bqliang.leavesheet.data.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.bqliang.leavesheet.LeaveSheet
import java.io.InputStream
import java.io.OutputStream

val Context.leaveSheetDataStore: DataStore<LeaveSheet> by dataStore(
    fileName = "leave_sheet.pb",
    serializer = LeaveSheetSerializer
)


private object LeaveSheetSerializer : Serializer<LeaveSheet> {

    override val defaultValue: LeaveSheet
        get() = LeaveSheet.newBuilder().apply {
            // 个人信息
            faculty = "电气与计算机工程学院"
            major = "软件工程"
            className = "XX软件X班"
            stuId = "1234567890"
            applicant = "张三"
            phone = "13800138000"

            // 请假信息
            applicationDate = 1666862636000L
            type = "病假"
            reason = "生病"
            startTime = 1666862636000L
            endTime = 1666862636000L
            durationInDays = 1f

            // 辅导员审核
            counselor = "李四"
            counselorAuditTime = 1666862636000L
            counselorAuditOpinion = "同意"

            // 系办公室审核
            facultyAuditor = "王五"
            facultyAuditTime = 1666862636000L
            facultyAuditOpinion = "同意"
        }.build()

    override suspend fun readFrom(input: InputStream): LeaveSheet {
        try {
            return LeaveSheet.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: LeaveSheet, output: OutputStream) =
        t.writeTo(output)
}