package cz.palda97.lpclient.view.editpipeline

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.Pipeline

class PipelineLayout @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    FrameLayout(context, attr, defStyleAttr, defStyleRes) {

    companion object {
        private val l = Injector.generateLogFunction(this)
        private const val STROKE_WIDTH: Float = 5.toFloat()
    }

    init {
        setWillNotDraw(false)
    }

    var currentPipeline: Pipeline? = null
    var componentsAndButtons: MutableMap<Component, View>? = null

    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = STROKE_WIDTH
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        val pipeline = currentPipeline ?: return
        val buttonMap = componentsAndButtons ?: return

        pipeline.connections.forEach {connection ->
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
                    canvas.drawLine(startX, startY, endX, endY, paint)
                }
            }
        }
    }
}