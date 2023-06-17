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

package com.media.sickle.base

import android.content.res.Configuration
import androidx.fragment.app.Fragment
import com.media.sickle.internal.SystemListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseFragment : Fragment(), CoroutineScope by MainScope() {
    protected var systemListener: SystemListener? = null

    fun registerSystemListener(listener: SystemListener) {
        this.systemListener = listener
    }

    private fun unRegisterSystemListener() {
        this.systemListener = null
    }


    override fun onDestroyView() {
        super.onDestroyView()
        unRegisterSystemListener()
        cancel()
    }
}