@file:Suppress("NAME_SHADOWING")

package cz.palda97.lpclient.view.editpipeline

import android.view.ViewGroup
import cz.palda97.lpclient.model.entities.pipeline.Component
import kotlin.math.roundToInt

object CoordinateConverter {

    private const val XSCALE: Float = 1.toFloat()
    private const val YSCALE: Float = 0.75.toFloat()
    private const val XOFFSET = 500
    private const val YOFFSET = 500
    const val DEFAULT_DENSITY: Float = 2.toFloat()

    fun toDisplay(x: Int, y: Int, density: Float?): Pair<Float, Float> {
        val density = density ?: DEFAULT_DENSITY
        return x * XSCALE * density to y * YSCALE * density
    }

    fun fromDisplay(x: Float, y: Float, density: Float?): Pair<Int, Int> {
        val density = density ?: DEFAULT_DENSITY
        return (x / (XSCALE * density)).roundToInt() to (y / (YSCALE * density)).roundToInt()
    }

    private fun toLayoutSize(x: Int, y: Int, density: Float?): Pair<Int, Int> {
        val density = density ?: DEFAULT_DENSITY
        val (x, y) = toDisplay(x, y, density)
        return (x + XOFFSET * density).roundToInt() to (y + YOFFSET * density).roundToInt()
    }

    fun ViewGroup.resize(components: List<Component>) {
        val maxX = components.maxBy {
            it.x
        }?.x ?: return
        val maxY = components.maxBy {
            it.y
        }?.y ?: return

        val params = layoutParams ?: return
        val (x, y) = toLayoutSize(maxX, maxY, resources?.displayMetrics?.density ?: DEFAULT_DENSITY)
        params.width = x
        params.height = y
        layoutParams = params
    }

    fun coordsToScrollTo(components: List<Component>, density: Float?): Pair<Int, Int>? {
        val density = density ?: DEFAULT_DENSITY
        val minX = components.minBy {
            it.x
        }?.x ?: return null
        val minY = components.minBy {
            it.y
        }?.y ?: return null

        return (minX * XSCALE * density).roundToInt() to (minY * YSCALE * density).roundToInt()
    }
}