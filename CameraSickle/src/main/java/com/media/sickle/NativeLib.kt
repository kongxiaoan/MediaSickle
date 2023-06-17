package com.media.sickle

class NativeLib {

    /**
     * A native method that is implemented by the 'sickle' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'sickle' library on application startup.
        init {
            System.loadLibrary("sickle")
        }
    }
}