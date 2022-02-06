package me.mervap.ai.home.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class LockableScrollView : ScrollView {
  constructor(context: Context?) : super(context)
  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  )

  private var isScrollable = true

  fun setScrollingEnabled(enabled: Boolean) {
    isScrollable = enabled
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(ev: MotionEvent): Boolean {
    return when (ev.action) {
      MotionEvent.ACTION_DOWN -> {
        if (isScrollable) {
          super.onTouchEvent(ev)
        }
        else {
          false
        }
      }
      else -> super.onTouchEvent(ev)
    }
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    return if (!isScrollable) false else super.onInterceptTouchEvent(ev)
  }
}