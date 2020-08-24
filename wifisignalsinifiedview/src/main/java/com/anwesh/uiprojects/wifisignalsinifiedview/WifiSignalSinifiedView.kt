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
val sweepDef : Float = 60f
val scGap : Float = 0.02f / (arcs + 1)
val strokeFactor : Float = 90f
val sizeFactor : Float = 4.5f
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 12

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
    paint.style = Paint.Style.STROKE
    drawWifiSinifiedSignal(scale, w, h, paint)
}

class WifiSignalSinifiedView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class WSSNode(var i : Int, val state : State = State()) {

        private var prev : WSSNode? = null
        private var next : WSSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = WSSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawWSSNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : WSSNode {
            var curr : WSSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class WifiSinifiedSignal(var i : Int) {

        private var curr : WSSNode = WSSNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : WifiSignalSinifiedView) {

        private val animator : Animator = Animator(view)
        private val wss : WifiSinifiedSignal = WifiSinifiedSignal(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            wss.draw(canvas, paint)
            animator.animate {
                wss.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            wss.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : WifiSignalSinifiedView {
            val view : WifiSignalSinifiedView = WifiSignalSinifiedView(activity)
            activity.setContentView(view)
            return view
        }
    }
}