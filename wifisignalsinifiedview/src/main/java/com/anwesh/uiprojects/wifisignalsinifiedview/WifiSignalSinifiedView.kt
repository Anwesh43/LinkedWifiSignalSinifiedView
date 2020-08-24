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
