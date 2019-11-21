package com.rifafauzi.customcamerasurfaceview.preview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

/**
 * Created by rrifafauzikomara on 2019-11-21.
 */

class Rectangle(context: Context) : View(context) {

    private var paint = Paint()

    override fun onDraw(canvas: Canvas) {
        paint.color = Color.GREEN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6F
        val rect = Rect(20, 885, 700, 465)
        canvas.drawRect(rect, paint)
    }
}