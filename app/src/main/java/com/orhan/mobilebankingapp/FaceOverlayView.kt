package com.orhan.mobilebankingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class FaceOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = android.graphics.Color.GREEN
        strokeWidth = 5f
    }

    private var faceRect: Rect? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val left = width / 4
        val top = height / 4
        val right = 3 * width / 4
        val bottom = 3 * height / 4
        faceRect = Rect(left, top, right, bottom)
        canvas.drawRect(faceRect!!, boxPaint)
    }
}
