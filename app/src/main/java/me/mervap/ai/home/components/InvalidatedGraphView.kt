package me.mervap.ai.home.components

import android.content.Context
import android.util.AttributeSet
import com.jjoe64.graphview.GraphView

class InvalidatedGraphView : GraphView {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
    attrs,
    defStyleAttr)

  fun invalidateSeries() {
    init()
  }
}