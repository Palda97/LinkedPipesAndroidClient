@file:Suppress("NAME_SHADOWING")

package cz.palda97.lpclient.view.editpipeline

import android.view.ViewGroup
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import kotlin.math.roundToInt

/**
 * Converter between display coordination and component's coordination.
 */
object CoordinateConverter {

    private const val XSCALE: Float = 1.toFloat()
    private const val YSCALE: Float = 0.75.toFloat()
    private const val XOFFSET = 500
    private const val YOFFSET = 500
    const val DEFAULT_DENSITY: Float = 2.toFloat()

    /**
     * Convert component's coordination into coordination to display.
     */
    fun toDisplay(x: Int, y: Int, density: Float?): Pair<Float, Float> {
        val density = density ?: DEFAULT_DENSITY
        return x * XSCALE * density to y * YSCALE * density
    }

    /**
     * Convert display coordination into component's coordination.
     */
    fun fromDisplay(x: Float, y: Float, density: Float?): Pair<Int, Int> {
        val density = density ?: DEFAULT_DENSITY
        return (x / (XSCALE * density)).roundToInt() to (y / (YSCALE * density)).roundToInt()
    }

    private fun toLayoutSize(x: Int, y: Int, density: Float?): Pair<Int, Int> {
        val density = density ?: DEFAULT_DENSITY
        val (x, y) = toDisplay(x, y, density)
        return (x + XOFFSET * density).roundToInt() to (y + YOFFSET * density).roundToInt()
    }

    /**
     * Sets the ViewGroup's size so it can display all the components.
     * @receiver Layout that need's to be resized.
     * @param components List of components that need to fit in this layout.
     */
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

    /**
     * Gets the coordination of component to scroll to.
     * @return Coordination to scroll to or null if the component list is empty.
     */
    fun coordsToScrollTo(components: List<Component>, density: Float?): Pair<Int, Int>? {
        val density = density ?: DEFAULT_DENSITY
        //val (minX, minY) = PipelineFactory.topLeftCoords(components) ?: return null
        val (minX, minY) = mostLeftComponent(components) ?: return null
        return (minX * XSCALE * density).roundToInt() to (minY * YSCALE * density).roundToInt()
    }

    private fun mostLeftComponent(components: List<Component>): Pair<Int, Int>? {
        var component = components.firstOrNull() ?: return null
        components.forEach {
            if (it.x < component.x) {
                component = it
            } else if (it.x == component.x && it.y < component.y) {
                component = it
            }
        }
        return component.x to component.y
    }

    private fun mostUpComponent(components: List<Component>): Pair<Int, Int>? {
        var component = components.firstOrNull() ?: return null
        components.forEach {
            if (it.y < component.y) {
                component = it
            } else if (it.y == component.y && it.x < component.x) {
                component = it
            }
        }
        return component.x to component.y
    }
}