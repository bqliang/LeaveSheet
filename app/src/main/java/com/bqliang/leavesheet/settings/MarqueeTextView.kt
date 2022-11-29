package com.bqliang.leavesheet.settings

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView

class MarqueeTextView : MaterialTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun isFocused() = true
}