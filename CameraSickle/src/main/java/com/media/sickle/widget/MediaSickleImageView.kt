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
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.media.sickle.NativeLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaSickleImageView(context: Context) : AppCompatImageView(context) {
    private var bitmap: Bitmap? = null

    fun bindBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    init {
        isClickable = true
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }

            MotionEvent.ACTION_MOVE -> {
                // 处理移动事件

                // 处理移动事件
                if (event.pointerCount == 2) {
                    // 获取两个手指的坐标
                    val x1 = event.getX(0)
                    val y1 = event.getY(0)
                    val x2 = event.getX(1)
                    val y2 = event.getY(1)

                    // 计算缩放值
                    val currentDistance: Float = getDistanceBetweenPoints(x1, y1, x2, y2)
                    val previousDistance: Float = getPreviousDistance(event)
                    val scale = currentDistance / previousDistance

                    // 处理缩放值
                    handleScale(scale)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getDistanceBetweenPoints(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    private fun getPreviousDistance(event: MotionEvent): Float {
        // 获取前一次事件的历史记录
        val historySize = event.historySize
        if (historySize > 0) {
            val x1 = event.getHistoricalX(0, historySize - 1)
            val y1 = event.getHistoricalY(0, historySize - 1)
            val x2 = event.getHistoricalX(1, historySize - 1)
            val y2 = event.getHistoricalY(1, historySize - 1)
            return getDistanceBetweenPoints(x1, y1, x2, y2)
        }
        return 0F
    }

    private fun handleScale(scale: Float) {
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