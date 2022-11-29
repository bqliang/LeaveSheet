package com.bqliang.leavesheet.data.datastore

import kotlinx.serialization.Serializable

@Serializable
data class LeaveSheet(
    // 个人信息
    val faculty: String,
    val major: String,
    val className: String,
    val stuId: String,
    val applicant: String,
    val phone: String,

    // 请假信息
    val applicationTime: Long,
    val type: String,
    val reason: String,
    val startTime: Long,
    val endTime: Long,
    val durationInDays: Float,

    // 辅导员审核
    val counselor: String,
    val counselorAuditTime: Long,
    val counselorAuditOpinion: String,

    // 系办公室审核
    val facultyAuditor: String,
    val facultyAuditTime: Long,
    val facultyAuditOpinion: String,
)
