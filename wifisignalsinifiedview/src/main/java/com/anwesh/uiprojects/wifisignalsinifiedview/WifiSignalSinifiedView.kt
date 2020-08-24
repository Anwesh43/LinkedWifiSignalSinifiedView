package com.anwesh.uiprojects.wifisignalsinifiedview

/**
 * Created by anweshmishra on 25/08/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.graphics.Canvas

val colors : Array<String> = arrayOf("#F44336", "#03A9F4", "#4CAF50", "#673AB7", "#009688")
val arcs : Int = 3
val startDeg : Float = -30f
val sweepDef : Float = 30f
val scGap : Float = 0.02f / (arcs + 1)
val strokeFactor : Float = 90f
val sizeFactor : Float = 4.5f
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawWifiSinifiedSignal(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val gap : Float = size / arcs
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, arcs + 1)
    save()
    translate(w / 2, h / 2)
    drawLine(0f, size * (1 - sf1), 0f, size, paint)
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
        for (i in 1..arcs) {
            val rGap : Float = gap * i
            val sfi : Float = sf.divideScale(i, arcs + 1)
            val deg : Float = sweepDef * sfi
            drawArc(RectF(-rGap, -rGap, rGap, rGap), startDeg + sweepDef / 2 - deg / 2, deg, false, paint)
        }
        restore()
    }
    restore()
}

fun Canvas.drawWSSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = Color.parseColor(colors[i])
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawWifiSinifiedSignal(scale, w, h, paint)
}

class WifiSignalSinifiedView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }
}