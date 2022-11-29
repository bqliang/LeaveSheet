package com.bqliang.leavesheet.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.bqliang.leavesheet.MyApp
import com.bqliang.leavesheet.R
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

val Context.leaveSheetDataStore: DataStore<LeaveSheet> by dataStore(
    fileName = "leave_sheet.json",
    serializer = LeaveSheetSerializer
)


private object LeaveSheetSerializer : Serializer<LeaveSheet> {

    override val defaultValue: LeaveSheet
        get() {
            val today = MaterialDatePicker.todayInUtcMilliseconds() // 早 8 点
            val yesterday = today - 1.days.inWholeMilliseconds
            val beforeYesterday = today - 2.days.inWholeMilliseconds

            return LeaveSheet(
                faculty = "电气与计算机工程学院",
                major = "软件工程",
                className = "XX软件X班",
                stuId = "1234567890",
                applicant = "张三",
                phone = "13800138000",

                applicationTime = beforeYesterday,
                type = MyApp.context.resources.getStringArray(R.array.leave_type_list)[0],
                reason = "生病",
                startTime = today,
                endTime = today + 10.hours.inWholeMilliseconds,
                durationInDays = 1.0f,

                counselor = "李四",
                counselorAuditTime = yesterday + 2.hours.inWholeMilliseconds,
                counselorAuditOpinion = "同意",

                facultyAuditor = "王五",
                facultyAuditTime = yesterday + 3.hours.inWholeMilliseconds,
                facultyAuditOpinion = "同意",
            )
        }


    override suspend fun readFrom(input: InputStream): LeaveSheet = try {
        Json.decodeFromString(
            deserializer = LeaveSheet.serializer(),
            input.bufferedReader().readText()
        )
    } catch (e: SerializationException) {
        Timber.e(e)
        defaultValue
    }


    override suspend fun writeTo(t: LeaveSheet, output: OutputStream) =
        output.bufferedWriter().use {
            it.write(
                Json.encodeToString(
                    serializer = LeaveSheet.serializer(),
                    value = t
                )
            )
        }
}