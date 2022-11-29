package com.bqliang.leavesheet.settings

import android.graphics.Canvas
import android.view.HapticFeedbackConstants
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.leavesheet.R
import com.google.android.material.color.MaterialColors
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlin.math.max
import kotlin.math.min

class SwipeToDeleteCallback(val onSwipe: (adapterPosition: Int) -> Unit) :
    ItemTouchHelper.SimpleCallback(0, LEFT) /* 不处理拖拽操作, 只处理左滑动 */ {

    private var oldDX = 0f

    companion object {
        private const val SWIPE_THRESHOLD = 0.3f
    }

    /**
     * 当 Item 被拖拽到新位置时回调. 不支持拖拽操作时，方法永远不会被调用
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) =
        onSwipe(viewHolder.absoluteAdapterPosition)


    /**
     * 用于设置滑动删除的阈值，当滑动距离超过阈值时，Item 就会被删除
     */
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = SWIPE_THRESHOLD


    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        // 震动反馈
        if (canvas.width * -SWIPE_THRESHOLD in (min(dX, oldDX)..max(dX, oldDX))) {
            recyclerView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        oldDX = dX

        RecyclerViewSwipeDecorator.Builder(
            canvas,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
            .addSwipeLeftBackgroundColor(
                MaterialColors.getColor(
                    viewHolder.itemView,
                    com.google.android.material.R.attr.colorError
                )
            )
            .addActionIcon(R.drawable.ic_outline_delete_24)
            .addSwipeLeftLabel("删除")
            .setSwipeLeftLabelColor(
                MaterialColors.getColor(
                    viewHolder.itemView,
                    com.google.android.material.R.attr.colorOnError
                )
            )
            .create()
            .decorate()
    }


    override fun getSwipeEscapeVelocity(defaultValue: Float) = defaultValue * 2
}