package com.bqliang.leavesheet.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bqliang.leavesheet.edit.EditActivity
import com.bqliang.leavesheet.edit.EditLeaveSheetFragment
import com.bqliang.leavesheet.edit.EditPersonalInfoFragment

class EditCollectionAdapter(editActivity: EditActivity): FragmentStateAdapter(editActivity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment =
        when(position) {
            0 ->  EditLeaveSheetFragment()
            1 ->  EditPersonalInfoFragment()
            else -> EditLeaveSheetFragment()
        }
}