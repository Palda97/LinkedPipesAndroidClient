package cz.palda97.lpclient.view.editpipeline

import android.view.ViewGroup
import cz.palda97.lpclient.model.entities.pipeline.Component
import kotlin.math.roundToInt

object CoordinateConverter {

    private const val XSCALE: Float = 2.toFloat()
    private const val YSCALE: Float = 1.5.toFloat()
    private const val XOFFSET = 1000
    private const val YOFFSET = 1000

    fun toDisplay(x: Int, y: Int): Pair<Float, Float> = x * XSCALE to y * YSCALE

    fun fromDisplay(x: Float, y: Float): Pair<Int, Int> = (x / XSCALE).roundToInt() to (y / YSCALE).roundToInt()

    private fun toLayoutSize(x: Int, y: Int): Pair<Int, Int> {
        val (x, y) = toDisplay(x, y)
        return (x + XOFFSET).roundToInt() to (y + YOFFSET).roundToInt()
    }

    fun ViewGroup.resize(components: List<Component>) {
        val maxX = components.maxBy {
            it.x
        }?.x ?: return
        val maxY = components.maxBy {
            it.y
        }?.y ?: return

        val params = layoutParams ?: return
        val (x, y) = toLayoutSize(maxX, maxY)
        params.width = x
        params.height = y
        layoutParams = params
    }

    fun coordsToScrollTo(components: List<Component>): Pair<Int, Int>? {
        val minX = components.minBy {
            it.x
        }?.x ?: return null
        val minY = components.minBy {
            it.y
        }?.y ?: return null

        return (minX * XSCALE).roundToInt() to (minY * YSCALE).roundToInt()
    }
}