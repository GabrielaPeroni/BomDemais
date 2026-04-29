package com.estoque.bomdemais.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.HapticFeedbackConstants
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R

class SwipeToDeleteCallback(
    private val isEnabled: () -> Boolean = { true },
    private val onSwiped: (position: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val background = ColorDrawable(Color.parseColor("#D32F2F"))
    private var hapticTriggered = false

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (isEnabled()) super.getSwipeDirs(recyclerView, viewHolder) else 0
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.5f

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        hapticTriggered = false
        onSwiped(viewHolder.bindingAdapterPosition)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        hapticTriggered = false
        super.clearView(recyclerView, viewHolder)
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val revealThreshold = itemView.width * 0.33f

        if (isCurrentlyActive && !hapticTriggered && Math.abs(dX) > revealThreshold) {
            itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            hapticTriggered = true
        }

        if (dX < 0) {
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            val icon = ContextCompat.getDrawable(recyclerView.context, R.drawable.ic_trash)
            if (icon != null) {
                val iconSize = icon.intrinsicHeight
                val iconMargin = (itemView.height - iconSize) / 2
                val revealedWidth = Math.abs(dX.toInt())
                val iconLeft = itemView.right - iconMargin - iconSize
                val iconRight = itemView.right - iconMargin
                if (revealedWidth > iconSize + iconMargin) {
                    icon.setBounds(iconLeft, itemView.top + iconMargin, iconRight, itemView.bottom - iconMargin)
                    icon.draw(c)
                }
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
