package cz.palda97.lpclient.view

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

object RecyclerViewCosmetics {

    /**
     * Adds divider between list items and adds swipe to delete functionality with red color and bin icon
     */
    fun <itemType> makeItAllWork(
        recyclerView: RecyclerView,
        getList: () -> List<itemType>?,
        deleteFunction: (itemType) -> Unit,
        context: Context
    ) {
        addSwipeToDeleteWithDecorations(
            recyclerView,
            getList,
            deleteFunction,
            context
        )
        addDividers(recyclerView, context)
    }

    fun <itemType> addSwipeToDeleteWithDecorations(
        recyclerView: RecyclerView,
        getList: () -> List<itemType>?,
        deleteFunction: (itemType) -> Unit,
        context: Context
    ) {
        val itemTouchHelper =
            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT + ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    getList()?.let {
                        deleteFunction(it[viewHolder.adapterPosition])
                    }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.delete_gesture_background
                            )
                        )
                        .addActionIcon(R.drawable.ic_baseline_delete_24)
                        .create()
                        .decorate()
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun addDividers(recyclerView: RecyclerView, context: Context) {
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }
}