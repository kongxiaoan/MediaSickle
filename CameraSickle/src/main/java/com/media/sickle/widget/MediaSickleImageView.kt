/*
 *  Copyright 2023 The MediaSickle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package com.media.sickle.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.media.sickle.NativeLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt


class MediaSickleImageView(context: Context) : AppCompatImageView(context) {
    private var bitmap: Bitmap? = null
    private var startPoint: PointF? = null
    private var startDistance = 0f
    private var scaleFactor = 0f
    private var matrix: Matrix? = null

    fun bindBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    init {
        isClickable = true
        matrix = Matrix()
        startPoint = PointF()
        startDistance = 0.0f
        scaleFactor = 1.0f
        scaleType = ScaleType.MATRIX
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> startPoint!![event.x] = event.y
            MotionEvent.ACTION_POINTER_DOWN -> startDistance = getDistance(event)
            MotionEvent.ACTION_MOVE -> if (event.pointerCount >= 2) {
                val currentDistance = getDistance(event)
                scaleFactor *= currentDistance / startDistance
                startDistance = currentDistance
                matrix?.reset();
                matrix?.postScale(scaleFactor, scaleFactor, getWidth() / 2f, getHeight() / 2f);
                setImageMatrix(matrix);
//                handleScale(scaleFactor)
            }
        }
        return true
    }

    private fun getDistance(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    private fun handleScale(scale: Float) {
        Log.d("handleScale", "scale = $scale")
        // 在此处处理缩放值
        // 可以将缩放值应用于自定义视图或执行其他操作
        bitmap?.let {
            MainScope().launch(Dispatchers.IO) {
                val scaleBitmapByBilinear = NativeLib.scaleBitmapByBilinear(it, scale)
                withContext(Dispatchers.Main) {
                    setImageBitmap(scaleBitmapByBilinear)
                }
            }
        }
    }
}