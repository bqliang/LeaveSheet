package com.bqliang.leavesheet.settings

import android.content.Context
import androidx.preference.SwitchPreferenceCompat
import com.bqliang.leavesheet.R


class MaterialSwitchPreference(context: Context) : SwitchPreferenceCompat(context) {

    init {
        widgetLayoutResource = R.layout.preference_widget_material_switch
    }
}