package com.bqliang.leavesheet.edit

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.bqliang.leavesheet.R
import com.bqliang.leavesheet.databinding.DialogDurationPickerBinding
import com.bqliang.leavesheet.databinding.FragmentEditLeaveSheetBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*


class EditLeaveSheetFragment : Fragment() {

    private val viewModel: EditViewModel by activityViewModels()
    private lateinit var binding: FragmentEditLeaveSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditLeaveSheetBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.editDuration) {
            inputType = InputType.TYPE_NULL
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    showDurationPickerDialog()
                    view.clearFocus()
                }
            }
        }

        with(binding.editType) {
            inputType = InputType.TYPE_NULL
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    showTypePickerDialog()
                    view.clearFocus()
                }
            }
        }

        binding.applicationDate.setOnClickListener {
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())

            showDatePickerDialog("选择申请日期", constraintsBuilder = constraintsBuilder) { newValue ->
                viewModel.applicationDate.value = newValue.timeInMillis
            }
        }

        binding.counselorAuditDate.setOnClickListener {
            showDatePickerDialog("选择辅导员审核日期") {
                val calendar = Calendar.getInstance()
                calendar[Calendar.YEAR] = it[Calendar.YEAR]
                calendar[Calendar.MONTH] = it[Calendar.MONTH]
                calendar[Calendar.DAY_OF_MONTH] = it[Calendar.DAY_OF_MONTH]
                viewModel.counselorAuditTime.value = calendar.timeInMillis
            }
        }

        binding.counselorAuditTime.setOnClickListener {
            showTimePickerDialog("选择辅导员审核时间") { hour, minute ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = viewModel.counselorAuditTime.value!!
                calendar[Calendar.HOUR_OF_DAY] = hour
                calendar[Calendar.MINUTE] = minute
                viewModel.counselorAuditTime.value = calendar.timeInMillis
            }
        }

        binding.facultyAuditDate.setOnClickListener {
            showDatePickerDialog("选择院系办公室审核日期") {
                val calendar = Calendar.getInstance()
                calendar[Calendar.YEAR] = it[Calendar.YEAR]
                calendar[Calendar.MONTH] = it[Calendar.MONTH]
                calendar[Calendar.DAY_OF_MONTH] = it[Calendar.DAY_OF_MONTH]
                viewModel.facultyAuditTime.value = calendar.timeInMillis
            }
        }

        binding.facultyAuditTime.setOnClickListener {
            showTimePickerDialog("选择院系办公室审核时间") { hour, minute ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = viewModel.facultyAuditTime.value!!
                calendar[Calendar.HOUR_OF_DAY] = hour
                calendar[Calendar.MINUTE] = minute
                viewModel.facultyAuditTime.value = calendar.timeInMillis
            }
        }

        binding.startDate.setOnClickListener {
            showDatePickerDialog("选择开始日期") {
                val calendar = Calendar.getInstance()
                calendar[Calendar.YEAR] = it[Calendar.YEAR]
                calendar[Calendar.MONTH] = it[Calendar.MONTH]
                calendar[Calendar.DAY_OF_MONTH] = it[Calendar.DAY_OF_MONTH]
                viewModel.startTime.value = calendar.timeInMillis
            }
        }

        binding.endDate.setOnClickListener {
            showDatePickerDialog("选择结束日期") {
                val calendar = Calendar.getInstance()
                calendar[Calendar.YEAR] = it[Calendar.YEAR]
                calendar[Calendar.MONTH] = it[Calendar.MONTH]
                calendar[Calendar.DAY_OF_MONTH] = it[Calendar.DAY_OF_MONTH]
                viewModel.endTime.value = calendar.timeInMillis
            }
        }

        binding.startTimeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.start_time_am -> {
                        changeAMOrPM(viewModel.startTime, Calendar.AM)
                    }
                    R.id.start_time_pm -> {
                        changeAMOrPM(viewModel.startTime, Calendar.PM)
                    }
                }
            }
        }

        binding.endTimeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.end_time_am -> {
                        changeAMOrPM(viewModel.endTime, Calendar.AM)
                    }
                    R.id.end_time_pm -> {
                        changeAMOrPM(viewModel.endTime, Calendar.PM)
                    }
                }
            }
        }
    }


    private fun showTypePickerDialog() {
        val curIndex = if (viewModel.type.value == "事假") 0 else 1
        val types = resources.getStringArray(R.array.list_leave_type)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("选择请假类型")
            .setSingleChoiceItems(types, curIndex) { dialog, _ ->
                val checkedItemPosition = (dialog as AlertDialog).listView.checkedItemPosition
                viewModel.type.value = types[checkedItemPosition]
                dialog.dismiss()
            }
            .show()
    }


    private fun showDurationPickerDialog() {
        val dialogBinding = DialogDurationPickerBinding.inflate(layoutInflater, null, false)
        val curInteger = viewModel.durationInDays.value?.toInt() ?: 0
        val curDecimal = viewModel.durationInDays.value?.minus(curInteger)?.times(10)?.toInt() ?: 0

        dialogBinding.apply {
            integerPicker.minValue = 0
            integerPicker.maxValue = 10
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
                    viewModel.durationInDays.value = "$integer.$decimal".toFloat()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }


    private fun showDatePickerDialog(
        title: String,
        selection: Long? = MaterialDatePicker.todayInUtcMilliseconds(),
        constraintsBuilder: CalendarConstraints.Builder = CalendarConstraints.Builder(),
        callback: (Calendar) -> Unit
    ) {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = today
        calendar[Calendar.MONTH] = Calendar.JANUARY
        val janThisYear = calendar.timeInMillis
        calendar[Calendar.MONTH] = Calendar.DECEMBER
        val decThisYear = calendar.timeInMillis

        constraintsBuilder
            .setStart(janThisYear)
            .setEnd(decThisYear)


        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(selection)
            .setCalendarConstraints(constraintsBuilder.build())
            .build()


        picker.addOnPositiveButtonClickListener {
            val newValue = Calendar.getInstance(Locale.CHINA)
            newValue.timeInMillis = it
            callback(newValue)
        }

        picker.show(parentFragmentManager, "date_picker")
    }


    private fun showTimePickerDialog(
        title: String,
        selection: Calendar = Calendar.getInstance(),
        callback: (Int, Int) -> Unit
    ) {
        val hour = selection[Calendar.HOUR_OF_DAY]
        val minute = selection[Calendar.MINUTE]

        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(title)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    callback(this.hour, this.minute)
                }
            }
            .show(parentFragmentManager, "time_picker")
    }


    private fun changeAMOrPM(time: MutableLiveData<Long>, flag: Int) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time.value!!
        calendar[Calendar.AM_PM] = flag
        time.value = calendar.timeInMillis
    }
}