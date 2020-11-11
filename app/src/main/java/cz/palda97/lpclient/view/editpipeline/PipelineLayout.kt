package cz.palda97.lpclient.view.editpipeline

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.color.MaterialColors
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.entities.pipeline.Vertex

class PipelineLayout @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    FrameLayout(context, attr, defStyleAttr, defStyleRes) {

    companion object {
        private val l = Injector.generateLogFunction(this)
        private const val STROKE_WIDTH: Float = 2.5.toFloat()
    }

    init {
        setWillNotDraw(false)
    }

    var currentPipeline: Pipeline? = null
    var componentsAndButtons: MutableMap<Component, View>? = null
    var vertexesAndButtons: MutableMap<Vertex, View>? = null

    val path = Path()

    private val paint = Paint().apply {
        color = MaterialColors.getColor(this@PipelineLayout, R.attr.textColorSecondary, Color.BLACK)
        strokeWidth = STROKE_WIDTH * (resources?.displayMetrics?.density ?: CoordinateConverter.DEFAULT_DENSITY)
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        val pipeline = currentPipeline ?: return
        val buttonMap = componentsAndButtons ?: return
        val vertexMap = vertexesAndButtons ?: return

        pipeline.connections.forEach { connection ->
            val sourceComponent = pipeline.components.find { it.id == connection.sourceComponentId }
            val targetComponent = pipeline.components.find { it.id == connection.targetComponentId }
            if (sourceComponent != null && targetComponent != null) {
                val sourceButton = buttonMap[sourceComponent]
                val targetButton = buttonMap[targetComponent]
                if (sourceButton != null && targetButton != null) {
                    val startX = sourceButton.x + sourceButton.width
                    val startY = sourceButton.y + sourceButton.height / 2.toFloat()
                    val endX = targetButton.x
                    val endY = targetButton.y + targetButton.height / 2.toFloat()
                    path.reset()
                    path.moveTo(startX, startY)
                    var bad = true
                    run vertexes@ {
                        connection.vertexIds.map {
                            pipeline.vertexes.find {vertex ->
                                vertex.id == it
                            } ?: return@vertexes
                        }.sortedBy {
                            it.order
                        }.forEach {
                            val vertexView = vertexMap[it] ?: return@vertexes
                            path.lineTo(vertexView.x + vertexView.width / 2.toFloat(), vertexView.y + vertexView.height / 2.toFloat())
                        }
                        bad = false
                    }
                    if (bad) {
                        path.reset()
                        path.moveTo(startX, startY)
                    }
                    path.lineTo(endX, endY)
                    canvas.drawPath(path, paint)
                    //canvas.drawLine(startX, startY, endX, endY, paint)
                }
            }
        }
    }
}