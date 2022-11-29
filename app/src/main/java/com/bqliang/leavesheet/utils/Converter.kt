@file:JvmName("Converter")

package com.bqliang.leavesheet.utils

import java.text.SimpleDateFormat
import java.util.*


fun dateFormat(timeInMillis: Long, withSuffix: Boolean = false): String {
    val date = Date(timeInMillis)
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    val simpleDateString = simpleDateFormat.format(date)

    return if (withSuffix) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val suffix = if (calendar[Calendar.AM_PM] == Calendar.AM) " 上午" else " 下午"
        simpleDateString + suffix
    } else {
        simpleDateString
    }
}


fun timeFormat(timeInMillis: Long): String {
    val date = Date(timeInMillis)
    val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
    return simpleDateFormat.format(date)
}


fun durationInDayFormat(durationInDays: Float): String {
    val integerPart = durationInDays.toInt()
    val decimalPart = ((durationInDays - integerPart) * 10).toInt()
    return "$integerPart" +
            if (decimalPart == 0) "" else ".$decimalPart"
}


fun auditDateTimeFormat(auditTime: Long): String {
    val date = Date(auditTime)
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    return simpleDateFormat.format(date)
}


fun isAM(timeInMillis: Long): Boolean {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeInMillis
    return calendar[Calendar.AM_PM] == Calendar.AM
}

