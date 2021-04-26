package cz.palda97.lpclient.view

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

/**
 * Class for tuning the recycler view.
 */
object RecyclerViewCosmetics {

    const val LEFT = ItemTouchHelper.LEFT
    const val RIGHT = ItemTouchHelper.RIGHT

    /**
     * Adds divider between list items and adds swipe to delete functionality with red color and bin icon.
     * @param recyclerView Recycler view to be tuned.
     * @param adapterWithList Adapter that will be assigned with the recycler view.
     * @param deleteFunction Delete function that will be used when swiping to the side.
     * @param context [Context].
     * @param dividers If there should be dividers between individual items.
     * @param swipeDirection Direction items will be allowed to be swiped to. Choose from [LEFT],
     * [RIGHT] and their combination ([LEFT] + [RIGHT]).
     */
    fun <itemType> makeItAllWork(
        recyclerView: RecyclerView,
        adapterWithList: AdapterWithList<itemType>,
        deleteFunction: (itemType) -> Unit,
        context: Context,
        dividers: Boolean = true,
        swipeDirection: Int = LEFT + RIGHT
    ) {
        attachAdapter(
            recyclerView,
            adapterWithList.adapter
        )
        addSwipeToDeleteWithDecorations(
            recyclerView,
            { adapterWithList.getList() },
            deleteFunction,
            context,
            swipeDirection
        )
        if (dividers)
            addDividers(recyclerView, context)
    }

    /**
     * Attach an adapter to the recycler view.
     * @param recyclerView Recycler view to be tuned.
     * @param adapter Adapter that will be assigned with the recycler view.
     */
    fun attachAdapter(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>
    ) {
        recyclerView.adapter = adapter
    }

    /**
     * Adds swipe to delete functionality with red color and bin icon.
     * @param recyclerView Recycler view to be tuned.
     * @param getList Function to get the content of the recycler view.
     * @param deleteFunction Delete function that will be used when swiping to the side.
     * @param context [Context].
     * @param swipeDir Direction items will be allowed to be swiped to. Choose from [LEFT],
     * [RIGHT] and their combination ([LEFT] + [RIGHT]).
     */
    fun <itemType> addSwipeToDeleteWithDecorations(
        recyclerView: RecyclerView,
        getList: () -> List<itemType>?,
        deleteFunction: (itemType) -> Unit,
        context: Context,
        swipeDir: Int
    ) {
        val itemTouchHelper =
            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, swipeDir) {
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

    /**
     * Adds divider between list items.
     * @param recyclerView Recycler view to be tuned.
     * @param context [Context].
     * [RIGHT] and their combination ([LEFT] + [RIGHT]).
     */
    fun addDividers(recyclerView: RecyclerView, context: Context) {
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }
}