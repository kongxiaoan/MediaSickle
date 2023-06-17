package com.media.sickle

import android.graphics.Bitmap

object NativeLib {

    /**
     * A native method that is implemented by the 'sickle' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    external fun scaleBitmapByBilinear(inputBitmap: Bitmap, scale: Float): Bitmap


    // Used to load the 'sickle' library on application startup.
    init {
        System.loadLibrary("sickle")
    }

}