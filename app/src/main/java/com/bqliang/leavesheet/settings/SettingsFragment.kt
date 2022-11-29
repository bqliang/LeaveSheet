package com.bqliang.leavesheet.settings

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import com.bqliang.leavesheet.BuildConfig
import com.bqliang.leavesheet.MyApp
import com.bqliang.leavesheet.R
import com.bqliang.leavesheet.collectLifecycleFlow
import com.bqliang.leavesheet.data.datastore.LeaveSheet
import com.bqliang.leavesheet.data.datastore.SettingsDataStore
import com.bqliang.leavesheet.data.datastore.leaveSheetDataStore
import com.bqliang.leavesheet.databinding.DialogAboutBinding
import com.bqliang.leavesheet.databinding.DialogDurationPickerBinding
import com.bqliang.leavesheet.utils.auditDateTimeFormat
import com.bqliang.leavesheet.utils.dateFormat
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.microsoft.appcenter.analytics.Analytics
import kotlinx.coroutines.launch
import rikka.html.text.toHtml
import rikka.preference.SimpleMenuPreference
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    private var applicationTime = 0L
    private var startTime = 0L
    private var endTime = 0L
    private var counselorAuditTime = 0L
    private var facultyAuditTime = 0L


    private fun Preference.setOnChangeListener(action: (LeaveSheet, Any) -> LeaveSheet) {
        setOnPreferenceChangeListener { _, newValue ->
            lifecycleScope.launch {
                MyApp.context.leaveSheetDataStore.updateData { leaveSheet ->
                    action(leaveSheet, newValue)
                }
            }
            true
        }
    }


    private fun saveTime(action: (LeaveSheet) -> LeaveSheet) = lifecycleScope.launch {
        MyApp.context.leaveSheetDataStore.updateData { leaveSheet ->
            action(leaveSheet)
        }
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireContext()
        context.setTheme(R.style.Theme_MyApp)
        val screen = preferenceManager.createPreferenceScreen(context)

        // 其他
        val facultyAuditVisible = SwitchPreference(context).apply {
            key = "faculty_audit_visible"
            title = "院系办公室审批流程"
            setOnPreferenceChangeListener { _, newValue ->
                lifecycleScope.launch {
                    SettingsDataStore.saveFacultyAuditVisible(newValue as Boolean)
                }
                true
            }
        }

        val about = Preference(context).apply {
            setIcon(R.drawable.ic_outline_info_24)
            title = "关于"
            summary = BuildConfig.VERSION_NAME
            setOnPreferenceClickListener {
                showAboutDialog()
                true
            }
        }

        // 申请信息
        val applicationDate = Preference(context).apply {
            key = "application_date"
            title = "申请日期"
            setOnPreferenceClickListener {
                val yesterday = Calendar.getInstance().apply {
                    add(Calendar.DATE, -2)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

                val constraintsBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                pickDate(
                    title = "请选择申请日期",
                    selectionUtc8 = yesterday,
                    constraintsBuilder = constraintsBuilder
                ) { newApplicationTime ->
                    saveTime {
                        it.copy(applicationTime = newApplicationTime.timeInMillis)
                    }
                }
                true
            }
        }

        val type = SimpleMenuPreference(context).apply {
            key = "type"
            title = "类型"
            entries = resources.getStringArray(R.array.leave_type_list)
            entryValues = resources.getStringArray(R.array.leave_type_list)
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(
                    type = (newValue as String)
                )
            }
        }

        val reason = EditTextPreference(context).apply {
            key = "reason"
            title = "请假理由"
            dialogTitle = "请假理由"
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(reason = newValue.toString())
            }
        }

        val annex = Preference(context).apply {
            key = "annex"
            title = "附件"
            fragment = "com.bqliang.leavesheet.settings.AnnexFragment"
        }

        val start = Preference(context).apply {
            key = "start"
            title = "开始日期"
            setOnPreferenceClickListener {
                pickDateTime("开始", startTime) { timeMillis ->
                    saveTime { leaveSheet ->
                        leaveSheet.copy(startTime = timeMillis)
                    }
                }
                true
            }
        }

        val end = Preference(context).apply {
            key = "end"
            title = "结束日期"
            setOnPreferenceClickListener {
                pickDateTime("结束", endTime) { timeMillis ->
                    saveTime { leaveSheet ->
                        leaveSheet.copy(endTime = timeMillis)
                    }
                }
                true
            }
        }

        val durationInDays = Preference(context).apply {
            key = "duration_in_days"
            title = "请假天数"
            setOnPreferenceClickListener {
                showDurationPickerDialog(this.summary.toString())
                true
            }
        }

        val counselor = EditTextPreference(context).apply {
            key = "counselor"
            title = "审核辅导员"
            dialogTitle = "审核辅导员"
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(counselor = newValue.toString())
            }
        }

        val counselorAuditOpinion = EditTextPreference(context).apply {
            key = "counselor_audit_opinion"
            title = "辅导员审核意见"
            dialogTitle = "辅导员审核意见"
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(counselorAuditOpinion = newValue.toString())
            }
        }

        val counselorAuditTime = Preference(context).apply {
            key = "counselor_audit_time"
            title = "辅导员审核时间"
            setOnPreferenceClickListener {
                pickDateTime("辅导员审核", counselorAuditTime) { timeMillis ->
                    saveTime { leaveSheet ->
                        leaveSheet.copy(counselorAuditTime = timeMillis)
                    }
                }
                true
            }
        }

        val facultyAuditor = EditTextPreference(context).apply {
            key = "faculty_auditor"
            title = "院系办公室审批人"
            dialogTitle = "院系办公室审批人"
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(facultyAuditor = newValue.toString())
            }
        }

        val facultyAuditOpinion = EditTextPreference(context).apply {
            key = "faculty_audit_opinion"
            title = "院系办公室审核意见"
            dialogTitle = "院系办公室审核意见"
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(facultyAuditOpinion = newValue.toString())
            }
        }

        val facultyAuditTime = Preference(context).apply {
            key = "faculty_audit_time"
            title = "院系办公室审核时间"
            setOnPreferenceClickListener {
                pickDateTime("院系办公室审核", facultyAuditTime) { timeMillis ->
                    saveTime { leaveSheet ->
                        leaveSheet.copy(facultyAuditTime = timeMillis)
                    }
                }
                true
            }
        }

        // 个人信息
        val faculty = SimpleMenuPreference(context).apply {
            key = "faculty"
            title = "院系名称"
            dialogTitle = "院系名称"
            setEntries(R.array.faculty_list)
            setEntryValues(R.array.faculty_list)
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(faculty = newValue as String)
            }
        }

        val major = EditTextPreference(context).apply {
            key = "major"
            title = "专业名称"
            dialogTitle = "专业名称"
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(major = newValue as String)
            }
        }

        val className = EditTextPreference(context).apply {
            key = "class_name"
            title = "班级名称"
            dialogTitle = "班级名称"
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(className = newValue as String)
            }
        }

        val applicant = EditTextPreference(context).apply {
            key = "applicant"
            title = "姓名"
            dialogTitle = "姓名"
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(applicant = newValue as String)
            }
        }

        val stuId = EditTextPreference(context).apply {
            key = "stu_id"
            title = "学号"
            dialogTitle = "学号"
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(stuId = newValue as String)
            }
        }

        val phone = EditTextPreference(context).apply {
            key = "phone"
            title = "手机号"
            dialogTitle = "手机号"
            setOnChangeListener { leaveSheet, newValue ->
                leaveSheet.copy(phone = newValue as String)
            }
        }

        val leaveSheet = PreferenceCategory(context).apply {
            setIcon(R.drawable.ic_round_receipt_long_24)
        }
        val personalInfo = PreferenceCategory(context).apply {
            setIcon(R.drawable.ic_outline_account_circle_24)
        }
        val other = PreferenceCategory(context)

        screen.apply {
            title = "设置"
            addPreference(leaveSheet)
            addPreference(personalInfo)
            addPreference(other)
            isPersistent = false
        }

        leaveSheet.apply {
            title = "请假条"
            addPreference(applicationDate)
            addPreference(type)
            addPreference(reason)
            addPreference(annex)
            addPreference(start)
            addPreference(end)
            addPreference(durationInDays)
            addPreference(counselor)
            addPreference(counselorAuditOpinion)
            addPreference(counselorAuditTime)
            addPreference(facultyAuditor)
            addPreference(facultyAuditOpinion)
            addPreference(facultyAuditTime)
        }

        personalInfo.apply {
            title = "个人信息"
            addPreference(faculty)
            addPreference(major)
            addPreference(className)
            addPreference(applicant)
            addPreference(stuId)
            addPreference(phone)
        }

        other.apply {
            title = "其他"
            addPreference(facultyAuditVisible)
            addPreference(about)
        }

        preferenceScreen = screen

        "faculty_audit_visible".let {
            facultyAuditor.dependency = it
            facultyAuditOpinion.dependency = it
            facultyAuditTime.dependency = it
        }


        collectLifecycleFlow(MyApp.context.leaveSheetDataStore.data) { it ->
            faculty.value = it.faculty
            faculty.summary = it.faculty
            major.text = it.major
            major.summary = it.major
            className.text = it.className
            className.summary = it.className
            applicant.text = it.applicant
            applicant.summary = it.applicant
            stuId.text = it.stuId
            stuId.summary = it.stuId
            phone.text = it.phone
            phone.summary = it.phone

            applicationDate.summary = dateFormat(it.applicationTime)
            type.summary = it.type
            reason.text = it.reason
            reason.summary = it.reason
            this@SettingsFragment.startTime = it.startTime
            start.summary = dateFormat(it.startTime, true)
            this@SettingsFragment.endTime = it.endTime
            end.summary = dateFormat(it.endTime, true)
            durationInDays.summary = it.durationInDays.toString()
            counselor.text = it.counselor
            counselor.summary = it.counselor
            counselorAuditOpinion.text = it.counselorAuditOpinion
            counselorAuditOpinion.summary = it.counselorAuditOpinion
            this@SettingsFragment.counselorAuditTime = it.counselorAuditTime
            counselorAuditTime.summary = auditDateTimeFormat(it.counselorAuditTime)
            facultyAuditor.text = it.facultyAuditor
            facultyAuditor.summary = it.facultyAuditor
            facultyAuditOpinion.text = it.facultyAuditOpinion
            facultyAuditOpinion.summary = it.facultyAuditOpinion
            this@SettingsFragment.facultyAuditTime = it.facultyAuditTime
            facultyAuditTime.summary = auditDateTimeFormat(it.facultyAuditTime)
        }

        collectLifecycleFlow(SettingsDataStore.facultyAuditVisible) { visible ->
            facultyAuditVisible.isChecked = visible
            facultyAuditVisible.setIcon(if (visible) R.drawable.ic_round_visibility_24 else R.drawable.ic_round_visibility_off_24)
            facultyAuditVisible.summary = "当前状态：${if (visible) "显示" else "隐藏"}"
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = Bundle().apply {
            putSerializable(
                SettingsActivity.TOOLBAR_TYPE,
                SettingsActivity.Toolbar.SETTINGS
            )
        }
        setFragmentResult(SettingsActivity.TOOLBAR_TYPE, bundle)
    }


    private fun pickDate(
        title: String,
        selectionUtc8: Long,
        constraintsBuilder: CalendarConstraints.Builder = CalendarConstraints.Builder(),
        positiveButtonText: String? = null,
        callback: (Calendar) -> Unit
    ) {
        val todayUtc = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = todayUtc
        calendar.add(Calendar.YEAR, -1)
        calendar[Calendar.MONTH] = Calendar.DECEMBER
        val decLastYear = calendar.timeInMillis
        calendar.add(Calendar.YEAR, 2)
        calendar[Calendar.MONTH] = Calendar.JANUARY
        val janLastYear = calendar.timeInMillis

        constraintsBuilder
            .setStart(decLastYear)
            .setEnd(janLastYear)

        /**
         * 分别用 UTC+8 和 UTC 时间戳表示同一个年月日时分秒，UTC > UTC+8
         * 例如要表示 2022-01-01 00:00:00
         * UTC+8 时间戳为  1640966400000
         * UTC 时间戳为    1640995200000 = (UTC+8) + 1000 * 60 * 60 * 8
         */
        val selectionUtc = selectionUtc8 + TimeZone.getDefault().rawOffset

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(selectionUtc)
            .setCalendarConstraints(constraintsBuilder.build())
            .setPositiveButtonText(positiveButtonText)
            .build()


        picker.addOnPositiveButtonClickListener { newTimeMillis ->
            val newCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                timeInMillis = newTimeMillis
            }

            val resultUtc8 = Calendar.getInstance().apply {
                clear()
                timeInMillis = selectionUtc8
                set(
                    newCalendar[Calendar.YEAR],
                    newCalendar[Calendar.MONTH],
                    newCalendar[Calendar.DAY_OF_MONTH]
                )
            }
            callback(resultUtc8)
        }
        picker.show(parentFragmentManager, "date_picker")
    }


    private fun pickTime(
        title: String,
        selectionUtc8: Long = System.currentTimeMillis(),
        callback: (Calendar) -> Unit
    ) {
        val selectionCalendarUtc8 = Calendar.getInstance().apply { timeInMillis = selectionUtc8 }
        val hour = selectionCalendarUtc8[Calendar.HOUR_OF_DAY]
        val minute = selectionCalendarUtc8[Calendar.MINUTE]

        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(title)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    val result = Calendar.getInstance()
                    result.timeInMillis = selectionCalendarUtc8.timeInMillis
                    result[Calendar.HOUR_OF_DAY] = this.hour
                    result[Calendar.MINUTE] = this.minute
                    callback(result)
                }
            }
            .show(parentFragmentManager, "time_picker")
    }


    private fun pickDateTime(
        timeDesc: String,
        selectionUtc8: Long,
        callback: (Long) -> Unit
    ) {
        val result = Calendar.getInstance()
        result.clear()

        pickDate(
            "请选择${timeDesc}日期",
            selectionUtc8,
            positiveButtonText = "下一步"
        ) { dateCalendarUtc8 ->
            result[Calendar.YEAR] = dateCalendarUtc8[Calendar.YEAR]
            result[Calendar.MONTH] = dateCalendarUtc8[Calendar.MONTH]
            result[Calendar.DAY_OF_MONTH] = dateCalendarUtc8[Calendar.DAY_OF_MONTH]

            pickTime("请选择${timeDesc}时间", selectionUtc8) { timeCalendarUtc8 ->
                result[Calendar.HOUR_OF_DAY] = timeCalendarUtc8[Calendar.HOUR_OF_DAY]
                result[Calendar.MINUTE] = timeCalendarUtc8[Calendar.MINUTE]
                result[Calendar.SECOND] = timeCalendarUtc8[Calendar.SECOND]
                callback(result.timeInMillis)
            }
        }
    }


    private fun showDurationPickerDialog(currentDurationStr: String) {
        val dialogBinding = DialogDurationPickerBinding.inflate(layoutInflater, null, false)
        val curInteger = currentDurationStr.split('.')[0].toInt()
        val curDecimal = currentDurationStr.split('.')[1].toInt()

        dialogBinding.apply {
            integerPicker.minValue = 0
            integerPicker.maxValue = 7
            integerPicker.value = curInteger
            decimalPicker.minValue = 0
            decimalPicker.maxValue = 1
            decimalPicker.displayedValues = arrayOf("0", "5")
            decimalPicker.value = if (curDecimal == 0) 0 else 1
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("请假时长")
            .setMessage("滚动滑轮选择请假时长")
            .setView(dialogBinding.root)
            .setPositiveButton("确定") { _, _ ->
                val integer = dialogBinding.integerPicker.value
                val decimal = if (dialogBinding.decimalPicker.value == 0) 0 else 5
                if (integer == 0 && decimal == 0) {
                    Toast.makeText(requireContext(), "时长不能设置为 0.0 天", Toast.LENGTH_SHORT).show()
                } else {
                    lifecycleScope.launch {
                        MyApp.context.leaveSheetDataStore.updateData {
                            it.copy(
                                durationInDays = "$integer.$decimal".toFloat()
                            )
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }


    fun showAboutDialog() {
        Analytics.trackEvent("About clicked")
        val packageManager = MyApp.context.packageManager
        val packageName = MyApp.context.packageName

        val appIcon = packageManager.getApplicationIcon(MyApp.context.packageName)
        val binding = DialogAboutBinding.inflate(layoutInflater, null, false)
        binding.apply {
            icon.setImageDrawable(appIcon)

            sourceCode.movementMethod = LinkMovementMethod.getInstance()
            sourceCode.text = getString(
                R.string.about_view_source_code,
                "<b><a href=\"https://github.com/bqliang/LeaveSheet\">GitHub</a></b>"
            ).toHtml()

            versionName.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName
            } else {
                @Suppress("deprecation")
                packageManager.getPackageInfo(packageName, 0).versionName
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
            .show()
    }
}