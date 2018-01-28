package com.gg.slidinglayout

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.HorizontalScrollView

/**
 * Creator : GG
 * Time    : 2018/1/14
 * Mail    : gg.jin.yu@gmail.com
 * Explain :
 */
class SlidingLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private lateinit var mMenuView: View
    private lateinit var mContentView: View
    private var mMenuWidth: Int

    private var mIsOpen = false

    private var mIntercept = false

    private val mGestureDetector: GestureDetector by lazy { GestureDetector(context, mGestureListener) }


    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.SlidingLayout)
        val rightMargin = array.getDimension(R.styleable.SlidingLayout_menuRightMargin, dip2px(50))
        mMenuWidth = (getScreenWidth(context) - rightMargin).toInt()
        array.recycle()
    }

    private val mGestureListener: GestureDetector.SimpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            Log.e("TAG", "velocityX -> " + velocityX)

            return when {
                !mIsOpen && velocityX > 0 -> {
                    openMenu()
                    true
                }
                mIsOpen && velocityX < 0 -> {
                    closeMenu()
                    true
                }
                else -> super.onFling(e1, e2, velocityX, velocityY)
            }

        }
    }

    private fun dip2px(dip: Int): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip.toFloat(), resources.displayMetrics)

    //    1.1 继承自定义 HorizontalScrollView  , 写个好两个布局（menu , content）, 运行起来看看效果
    //    1.2 运行之后布局是全部乱套的，menu ，content 宽度不对，应对方法:指定内容（屏幕的宽度）和菜单的宽度（屏幕的宽度 - 一段距离(自定义属性)）
    //    1.3 默认是关闭的，手指的抬起的时候要判断是关闭还是打开状态，采用代码滚动到对应位置
    //    1.4 处理快速滑动
    //    1.5 处理内容部分缩放，菜单部分有位移和透明度，时时刻刻监听当前滚动的位置
    //    1.6 充分考虑前几次看的源码（理论 + 实战）

    override fun onFinishInflate() {
        super.onFinishInflate()
        val container = getChildAt(0) as ViewGroup
        if (container.childCount != 2)
            throw RuntimeException("The parentView must contains two View")

        mMenuView = container.getChildAt(0)
        //通过设置LayoutParams 来设置宽度
        val mMenuParams = mMenuView.layoutParams
        mMenuParams.width = mMenuWidth
        mMenuView.layoutParams = mMenuParams

        mContentView = container.getChildAt(1)

        val mContentParams = mContentView.layoutParams
        mContentParams.width = getScreenWidth(context)
        mContentView.layoutParams = mContentParams
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        scrollTo(mMenuWidth, 0)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val scale = l / mMenuWidth.toFloat()
        val rightScale = 0.7f + 0.3f * scale

        mContentView.scaleX = rightScale
        mContentView.scaleY = rightScale
        mContentView.pivotX = 0f
        mContentView.pivotY = mContentView.measuredHeight / 2f

        val leftScale = 0.7f + (1 - scale) * 0.3f
        mMenuView.scaleX = leftScale
        mMenuView.scaleY = leftScale

        mMenuView.alpha = leftScale

        mMenuView.translationX = 0.25f * l

    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        mIntercept = false
        if (mIsOpen) {
            if (ev.x > mMenuWidth) {
                closeMenu()
                mIntercept = true
                return true
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (mIntercept)
            return true
        if (mGestureDetector.onTouchEvent(ev))
            return true

        if (ev.action == MotionEvent.ACTION_UP) {
            if (scrollX > mMenuWidth / 2) {
                closeMenu()
            } else {
                openMenu()
            }

            return true
        }
        return super.onTouchEvent(ev)
    }

    private fun closeMenu() {
        smoothScrollTo(mMenuWidth, 0)
        mIsOpen = false
    }

    private fun openMenu() {
        smoothScrollTo(0, 0)
        mIsOpen = true
    }


    private fun getScreenWidth(context: Context): Int {
        val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }
}