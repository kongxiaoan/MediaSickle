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

package com.media.sickle

import android.content.Context
import android.content.Intent
import com.media.sickle.utils.MediaSickleUtils
import java.io.File

const val DEFAULT_NAME = "MediaSickle"

/**
 * 对外提供功能
 */
class MediaSickle private constructor(builder: Builder) {

    var mContext: Context = builder.getContext()

    var outputDirectory = builder.getOutputDirectory()

    var projectName = builder.getProjectName()


    fun openCamera(context: Context) {
        context.startActivity(Intent(context, MediaSickleActivity::class.java))
    }

    companion object {

        private lateinit var INSTANCE: MediaSickle
        private fun init(mediaSickle: MediaSickle) {
            if (!isInstalled()) {
                this.INSTANCE = mediaSickle
            } else {
                throw RuntimeException("已经初始化")
            }
        }

        internal fun isInstalled() = this::INSTANCE.isInitialized

        fun with(): MediaSickle {
            if (!isInstalled()) {
                throw RuntimeException("未初始化")
            }
            return INSTANCE
        }
    }

    class Builder(private val context: Context) {

        private var projectName: String = DEFAULT_NAME


        private var outputDirectory: File =
            MediaSickleUtils.getOutputDirectory(context, DEFAULT_NAME)

        fun withOutputDirectory(outputDirectory: File) = apply {
            this.outputDirectory = outputDirectory
        }

        fun withProjectName(projectName: String) = apply {
            this.projectName = projectName
        }

        fun getProjectName() = projectName

        fun getOutputDirectory() = outputDirectory

        fun getContext() = context

        fun build() {
            init(MediaSickle(this))
        }
    }

}