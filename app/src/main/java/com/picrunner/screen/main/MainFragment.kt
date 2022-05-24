package com.picrunner.screen.main

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.picrunner.BuildConfig
import com.picrunner.R
import com.picrunner.databinding.FragmentMainBinding
import com.picrunner.service.LocationService
import com.picrunner.util.isServiceRunning
import com.picrunner.util.isServiceRunningInForeground
import com.picrunner.util.showEndlessSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        private const val TAG = "MainFragment"
    }

    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentMainBinding? = null
    val binding: FragmentMainBinding
        get() = _binding!!

    private var locationService: LocationService? = null

    private var locationReceiver: LocationReceiver? = null
    private var locationServiceStateReceiver: LocationServiceStateReceiver? = null

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(LocationService.TAG, "Background service connected.")
            val binder = service as LocationService.LocationBinder
            locationService = binder.service
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

    init {
        locationReceiver = LocationReceiver()
        locationServiceStateReceiver = LocationServiceStateReceiver()
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(
            Intent(context, LocationService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        /** These registered receivers will live as long as application will due to
         application context. According to the documentation there is no necessity
        to unregister if we want them to live as long as activity will. */
        locationReceiver?.let {
            LocalBroadcastManager.getInstance(requireActivity().applicationContext)
                .registerReceiver(it, IntentFilter(LocationService.ACTION_LOCATION_BROADCAST))
        }
        locationServiceStateReceiver?.let {
            LocalBroadcastManager.getInstance(requireActivity().applicationContext)
                .registerReceiver(it, IntentFilter(LocationService.ACTION_SERVICE_STATE_BROADCAST))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnStart.setOnClickListener {
            locationService?.requestLocationUpdates()
            viewModel.startWalk()
        }
        binding.btnStop.setOnClickListener {
            locationService?.cancelLocationUpdates()
            viewModel.stopWalk()
        }
        lifecycleScope.launchWhenCreated {
            viewModel.isLocationDetectionInProgress.collect { inProgress ->
                if (inProgress) {
                    binding.btnStart.visibility = View.GONE
                    binding.btnStop.visibility = View.VISIBLE
                } else {
                    binding.btnStart.visibility = View.VISIBLE
                    binding.btnStop.visibility = View.GONE
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.imagesFlow.collect { image ->
                Toast.makeText(context, image.toString(), Toast.LENGTH_LONG).show()
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

    inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(LocationService.KEY_EXTRA_LOCATION)
            if (location != null) {
                viewModel.locationFlow.trySend(location)
            }
            Toast.makeText(context, location.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    inner class LocationServiceStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val isRunning = intent.getBooleanExtra(LocationService.KEY_EXTRA_SERVICE_STATE, false)
            lifecycleScope.launch {
                viewModel.isLocationDetectionInProgress.emit(isRunning)
            }
        }
    }
}