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

package com.media.sickle.camera

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.media.sickle.KEY_EVENT_ACTION
import com.media.sickle.KEY_EVENT_EXTRA
import com.media.sickle.R
import com.media.sickle.base.BaseFragment
import com.media.sickle.databinding.CameraUiControllerBinding
import com.media.sickle.databinding.FragmentCameraBinding
import com.media.sickle.emnus.CameraControlsType
import com.media.sickle.permission.PermissionFragment
import com.media.sickle.utils.CameraXManager
import com.media.sickle.utils.MediaSickleUtils
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class CameraFragment : BaseFragment() {
    private var _binding: FragmentCameraBinding? = null

    private var cameraUiControllerBinding: CameraUiControllerBinding? = null
    private val binding get() = _binding!!

    private val cameraXManager by lazy {
        CameraXManager(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewFinder.post {
            updateCameraUI()
            setUpCamera()
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        systemListener?.onConfigurationChanged(newConfig)
    }


    private fun setUpCamera() {
        cameraXManager.startCamera(binding.viewFinder, CameraControlsType.TAKE_PICTURE)
    }

    private fun updateCameraUI() {
        cameraUiControllerBinding?.root?.let {
            binding.root.removeView(it)
        }

        cameraUiControllerBinding = CameraUiControllerBinding.inflate(
            LayoutInflater.from(requireContext()),
            binding.root,
            true
        )
        cameraXManager.updateController(cameraUiControllerBinding!!)
        lifecycle.addObserver(cameraXManager)
        registerSystemListener(cameraXManager)
    }

    override fun onResume() {
        super.onResume()
        /**
         * 检测权限是否存在 不存在时请求权限
         */
        if (!PermissionFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                CameraFragmentDirections.actionCameraToPermissions()
            )
        }
    }


    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}