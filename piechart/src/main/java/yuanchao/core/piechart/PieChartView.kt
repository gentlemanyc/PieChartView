package yuanchao.core.piechart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.OvershootInterpolator
import java.util.*


class PieChartView : View {

    /**
     * 主绘制区域.
     */
    private val mainRect = RectF()

    /**
     * 中间圆的path
     */
    private val centerCirclePath = Path()

    private var centerCirclePaint: Paint? = null

    /**
     *中间圆的半径
     */
    private var centerCircleRadius = 0F

    /**
     * 中间圆的颜色
     * 如果没有设置颜色,则中间的圆将会裁剪绘制区域.(中间圆的绘制区域会被掏空)
     */
    private var centerCircleColor = -1

    /**
     * 是否展示动画
     */
    private var animEnable = true

    /**
     * 绘制的弧形列表.
     */
    private val pathList = arrayListOf<PathParams>()

    /**
     * 适配器.
     */
    var adapter: PieChartAdapter? = null
        set(value) {
            field = value
            value?.let {
                pathList.clear()
                totalRange = it.getTotalValue()
                for (i in 0 until it.getCount()) {
                    val value = it.getValue(i)
                    if (value > 0) {
                        val pathParams = PathParams()
                        pathParams.range = value
                        pathList.add(pathParams)
                        pathParams.paint.color = it.getColor(i)
                    }
                }
                isSingleFull = pathList.size == 1 && pathList[0].range == totalRange
            }
            if (animEnable && !isInEditMode) {
                val a = ValueAnimator.ofFloat(1F, 360F).setDuration(2000)
                a.addUpdateListener {
                    maxRange = it.animatedValue as Float
                    invalidate()
                }
                a.interpolator = OvershootInterpolator()
                a.start()
            } else {
                invalidate()
            }
        }

    /**
     * 最大展示的角度.默认为整圆(比如你将它的值设置为180F那么所有绘制的弧形只占半个圆)
     */
    private var maxRange = 360F

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }


    private fun init(context: Context, attrs: AttributeSet?) {

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView)
            centerCircleRadius = typedArray.getDimension(R.styleable.PieChartView_pct_center_circle_radius, 0F)
            centerCircleColor = typedArray.getColor(R.styleable.PieChartView_pct_center_circle_color, -1)
            if (centerCircleColor != -1) {
                centerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
                centerCirclePaint!!.color = centerCircleColor;
            }
            animEnable = typedArray.getBoolean(R.styleable.PieChartView_pct_anim_enable, !isInEditMode)
            typedArray.recycle()
        }

        if (isInEditMode) {
            adapter = object : PieChartAdapter {
                val floatArray = floatArrayOf(25F, 25F, 25F, 25F)
                override fun getTotalValue(): Float {
                    return floatArray.sum()
                }

                override fun getCount(): Int {
                    return floatArray.count()
                }

                override fun getColor(position: Int): Int {
                    return Color.rgb(Random().nextInt(255), Random().nextInt(255), Random().nextInt(255))
                }

                override fun getValue(position: Int): Float {
                    return floatArray[position]
                }
            }
        }

    }

    private fun getSize(ms: Int): Int {
        val mode = MeasureSpec.getMode(ms)
        when (mode) {
            MeasureSpec.EXACTLY -> return MeasureSpec.getSize(ms)
            else -> return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                if (minimumWidth == 0) 100F else minimumWidth.toFloat()
                , resources.displayMetrics
            ).toInt()
        }
    }

    override fun getMinimumWidth(): Int {
        return super.getMinimumWidth()
    }

    private var totalRange: Float = 0F
    private var isSingleFull = false
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(getSize(widthMeasureSpec), getSize(heightMeasureSpec))
        mainRect.set(0F, 0F, measuredWidth.toFloat(), measuredHeight.toFloat())
        centerCirclePath.reset()
        centerCirclePath.addCircle(
            mainRect.centerX(),
            mainRect.centerY(),
            //默认半径为总绘制获取宽度的1/4
            if (centerCircleRadius == 0F) mainRect.width() / 4 else centerCircleRadius,
            Path.Direction.CCW
        )
        //预览效果
        if (isInEditMode) {
            adapter?.let {
                pathList.clear()
                for (i in 0 until it.getCount()) {
                    totalRange = it.getTotalValue()
                    val pathParams = PathParams()
                    pathParams.range = it.getValue(i)
                    pathList.add(pathParams)
                    pathParams.paint.color = it.getColor(i)
                }
            }
        }
    }

    private val xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //离屏缓冲。
        val saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
        var curRange = 0F

        //中间部分,没有颜色,默认将裁剪它的区域.
        if (centerCircleColor == -1) {
            for (p in pathList) {
                p.path.reset()
                val range = p.range / totalRange * maxRange
                p.path.moveTo(mainRect.centerX(), mainRect.centerY())
                if (isSingleFull) {
                    p.path.addCircle(mainRect.centerX(), mainRect.centerY(), mainRect.width() / 2, Path.Direction.CCW)
                } else {
                    p.path.arcTo(mainRect, curRange, range, false)
                }
                canvas.drawPath(p.path, p.paint)
                p.paint.xfermode = xfermode
                canvas.drawPath(centerCirclePath, p.paint)
                p.paint.xfermode = null
                curRange += range
            }
        } else {
            //中间部分有颜色,直接绘制
            for (p in pathList) {
                p.path.reset()
                val range = p.range / totalRange * maxRange
                p.path.moveTo(mainRect.centerX(), mainRect.centerY())
                p.path.arcTo(mainRect, curRange, range, false)
                canvas.drawPath(p.path, p.paint)
                canvas.drawPath(centerCirclePath, centerCirclePaint!!)
                curRange += range
            }
        }
        canvas.restoreToCount(saved);
    }

    internal class PathParams {
        var path = Path()
        var paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var range: Float = 0F
    }

    interface PieChartAdapter {

        fun getValue(position: Int): Float

        fun getColor(position: Int): Int

        fun getCount(): Int

        fun getTotalValue(): Float
    }
}

