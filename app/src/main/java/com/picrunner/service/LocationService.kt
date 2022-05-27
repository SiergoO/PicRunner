package com.picrunner.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.picrunner.MainActivity
import com.picrunner.R
import com.picrunner.domain.model.Photo
import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.usecase.search.DeleteAllPhotosFromDBUseCaseParam
import com.picrunner.domain.usecase.search.DeleteAllPhotosFromDBUseCaseResult
import com.picrunner.domain.usecase.search.GetNearestPhotoParam
import com.picrunner.domain.usecase.search.GetNearestPhotoResult
import com.picrunner.util.isServiceRunningInForeground
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationService @Inject constructor() : LifecycleService() {

    companion object {

        const val TAG = "LocationService"

        private const val NOTIFICATION_CHANNEL_ID = "locationNotificationChannel"
        private const val NOTIFICATION_ID = 1
        private const val KEY_EXTRA_LAUNCHED_FROM_NOTIFICATION = "launchedFromNotification"

        private const val SMALLEST_DISPLACEMENT_IN_METERS = 100F
        private const val INTERVAL_TIME_IN_MILLIS = 25_000L
        private const val FASTEST_INTERVAL_TIME_IN_MILLIS = 15_000L
    }

    @Inject
    lateinit var getNearestPhotoUseCase: UseCase<GetNearestPhotoParam, GetNearestPhotoResult>
    @Inject
    lateinit var deletePhotosFromDBUseCase:
            UseCase<DeleteAllPhotosFromDBUseCaseParam, DeleteAllPhotosFromDBUseCaseResult>

    private val serviceJob = SupervisorJob()
    private val locationServiceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var locationJob: Job? = null

    private val binder = LocationBinder()

    private val locationFlow = MutableStateFlow<List<Location>>(listOf())

    private val _isLocationDetectionInProgress = MutableStateFlow(false)
    val isLocationDetectionInProgress = _isLocationDetectionInProgress.asStateFlow()

    private val _photoFlow = MutableStateFlow<Photo?>(null)
    val photoFlow = _photoFlow.asStateFlow()

    private val _errorChannel = Channel<Throwable>(Channel.UNLIMITED)
    val errorFlow: Flow<Throwable> = _errorChannel.receiveAsFlow()

    private var locationRequest: LocationRequest? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationServiceHandler: Handler? = null
    private var location: Location? = null
    private var notificationManager: NotificationManager? = null
    private var connectivityManager: ConnectivityManager? = null

    private var locationCallback: LocationCallback? = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            onNewLocation(locationResult.lastLocation)
        }
    }

    private var networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            locationJob?.cancel()
            locationJob = null
            getUndeliveredPhotos()
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        getLastLocation()

        //Creating handler to control over callbacks and messages
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        locationServiceHandler = Handler(handlerThread.looper)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Creating notification channel in case we targeting "O" SDK and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.app_name)
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager?.createNotificationChannel(channel)
        }

        connectivityManager?.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Location service started")
        val startedFromNotification = intent?.getBooleanExtra(
            KEY_EXTRA_LAUNCHED_FROM_NOTIFICATION,
            false
        )

        // If user decides to stop location updates from the notification.
        if (startedFromNotification == true) {
            stopSelf()
            cancelLocationUpdates()
        }
        // For service not to be recreated after killing.
        return super.onStartCommand(intent, flags, START_NOT_STICKY)
    }

    // When activity comes to foreground this method is being called
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.i(TAG, "binding service")
        stopForeground(true)
        return binder
    }

    override fun onRebind(intent: Intent) {
        // Called when a client (WalkActivity in case of this sample) returns to the foreground and
        // binds once again with this service. The service should be launched as foreground one.
        Log.i(TAG, "rebinding service")
        stopForeground(true)
        onLocationServiceStateChanged(true)
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.i(TAG, "Unbound from service and starting foreground")
        startForeground(NOTIFICATION_ID, getNotification())
        onLocationServiceStateChanged(false)
//        locationJob?.cancel()
//        locationJob = null
        return true
    }

    override fun onDestroy() {
        locationServiceHandler?.removeCallbacksAndMessages(null)
        connectivityManager?.unregisterNetworkCallback(networkCallback)
        serviceJob.cancel()
        locationJob?.cancel()
        locationJob = null
        super.onDestroy()
    }

    // Requesting for location updates somewhere outside and starting location service
    fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")
        startService(Intent(applicationContext, LocationService::class.java))
        try {
            if (locationRequest != null && locationCallback != null) {
                fusedLocationProviderClient?.requestLocationUpdates(
                    locationRequest!!, locationCallback!!, Looper.myLooper() ?: Looper.getMainLooper()
                )
            }
            locationServiceScope.launch {
                deletePhotosFromDBUseCase.execute(DeleteAllPhotosFromDBUseCaseParam)
            }
        } catch (exception: SecurityException) {
            Toast.makeText(
                applicationContext,
                "Provide location permission to make the app work",
                Toast.LENGTH_LONG
            ).show()
            Log.e(TAG, exception.message.toString())
        }
        onLocationServiceStateChanged(true)
    }

    // Requesting for cancelling location updates somewhere outside and stopping location service
    fun cancelLocationUpdates() {
        Log.i(TAG, "Removing location updates")
        try {
            locationCallback?.let { fusedLocationProviderClient?.removeLocationUpdates(it) }
            locationJob?.cancel()
            locationJob = null
            stopSelf()
            onLocationServiceStateChanged(false)
        } catch (exception: SecurityException) {
            Log.e(TAG, exception.message.toString())
        }
    }

    private fun getLastLocation() {
        try {
            fusedLocationProviderClient?.lastLocation?.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    location = task.result!!
                } else {
                    Log.w(TAG, "Location request failed.")
                }
            }
        } catch (exception: SecurityException) {
            Log.e(TAG, exception.message.toString())
        }
    }

    private fun onLocationServiceStateChanged(isRunning: Boolean) {
        locationServiceScope.launch {
            _isLocationDetectionInProgress.emit(isRunning)
        }
    }

    private fun onNewLocation(location: Location) {
        Log.i(TAG, "New location: ${location.latitude}, ${location.longitude}")
        this.location = location

        //Fetching photo on new location
        locationServiceScope.launch {
            getPhoto(location)
        }

        if (this@LocationService.isServiceRunningInForeground()) {
            notificationManager?.notify(NOTIFICATION_ID, getNotification())
        }
    }

    private fun getUndeliveredPhotos() {
        if (locationJob?.isActive != true) {
            locationJob = locationServiceScope.launch {
                locationFlow
                    .asStateFlow()
                    .runningReduce {accumulator, value -> accumulator - value.toSet() }
                    .collectLatest { locations ->
                    locations.forEach { getPhoto(it) }
                }
            }
        }
    }

    private suspend fun getPhoto(location: Location) {
        getNearestPhotoUseCase.execute(
            GetNearestPhotoParam(Pair(location.latitude, location.longitude))
        ).fold(
            onSuccess = { photo ->
                _photoFlow.value = photo
            },
            onFailure = { error ->
                //Emitting location to get undelivered photos later
                locationFlow.emit(locationFlow.value + location)
                _errorChannel.trySend(error)
            }
        )
    }

    private fun createLocationRequest() {
        locationRequest = create().apply {
            priority = PRIORITY_HIGH_ACCURACY
            smallestDisplacement = SMALLEST_DISPLACEMENT_IN_METERS
            interval = INTERVAL_TIME_IN_MILLIS
            fastestInterval = FASTEST_INTERVAL_TIME_IN_MILLIS
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getNotification(): Notification {
        val intent = Intent(this, LocationService::class.java)

        // Intent Extra to detect whether we came in onStartCommand via the notification or not.
        intent.putExtra(KEY_EXTRA_LAUNCHED_FROM_NOTIFICATION, true)

        // PendingIntent that calls onStartCommand() in this service.
        val servicePendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        // PendingIntent to launch main activity.
        val activityPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                0
            )
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .addAction(0, getString(R.string.notification_action_go_to_app), activityPendingIntent)
            .addAction(0, getString(R.string.notification_action_stop), servicePendingIntent)
            .setContentTitle(getString(R.string.notification_content_title))
            .setContentText(getString(R.string.notification_content_text))
            .setOngoing(true)
            .setPriority(1)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setWhen(System.currentTimeMillis())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_CHANNEL_ID)
        }

        return builder.build()
    }

    inner class LocationBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }
}