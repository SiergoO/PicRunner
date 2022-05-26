package com.picrunner.screen.main

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.picrunner.BuildConfig
import com.picrunner.R
import com.picrunner.databinding.FragmentWalkBinding
import com.picrunner.service.LocationService
import com.picrunner.util.showEndlessSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalkFragment : Fragment() {

    companion object {
        private const val TAG = "MainFragment"
    }

    private val viewModel: WalkViewModel by viewModels()
    private var _binding: FragmentWalkBinding? = null
    val binding: FragmentWalkBinding
        get() = _binding!!

    private var walkAdapter: WalkAdapter? = null

    private var locationService: LocationService? = null

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(LocationService.TAG, "Background service connected.")
            val binder = service as LocationService.LocationBinder
            locationService = binder.service
            lifecycleScope.launchWhenCreated {
                binder.service.isLocationDetectionInProgress.collect {
                    binding.btnStart.isVisible = !it
                    binding.btnStop.isVisible = it
                }
            }
            viewModel.sendLocation(binder.service.locationFlow)
            requestPermission()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(LocationService.TAG, "Background service disconnected.")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Permission granted")
        } else {
            requireView()
                .showEndlessSnackbar(getString(R.string.location_permission_settings_text))
                .setAction(getString(R.string.location_permission_settings_text)) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }.show()
            Log.d(TAG, "Permission denied")
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(
            Intent(context, LocationService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalkBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        walkAdapter = WalkAdapter(Glide.with(requireContext()))
        binding.listLocationPhotos.apply {
            val llm = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                true
            )
            llm.stackFromEnd = true
            layoutManager = llm
            adapter = walkAdapter
        }
        binding.btnStart.setOnClickListener {
            locationService?.run {
                requestLocationUpdates()
            }
        }
        binding.btnStop.setOnClickListener {
            locationService?.cancelLocationUpdates()
        }
        lifecycleScope.launchWhenCreated {
            viewModel.photoUriListFlow.collect { photoUrlList ->
                walkAdapter?.submitList(photoUrlList)
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.errorFlow.collect { error ->
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        _binding = null
        walkAdapter = null
        super.onDestroyView()
    }

    override fun onStop() {
        requireActivity().unbindService(serviceConnection)
        super.onStop()
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ->
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(
                    this.requireView(),
                    getString(R.string.location_permission_text),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(getString(R.string.location_permission_ok_text)) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }.show()
            }
            else -> {
                // Do nothing
            }
        }
    }
}