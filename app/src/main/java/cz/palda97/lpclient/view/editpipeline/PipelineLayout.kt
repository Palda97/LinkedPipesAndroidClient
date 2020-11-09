package cz.palda97.lpclient.view.editpipeline

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.palda97.lpclient.Injector

class PipelineLayout @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    FrameLayout(context, attr, defStyleAttr, defStyleRes) {

    companion object {
        private val l = Injector.generateLogFunction(this)
    }

    init {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        //
    }
}