package com.picrunner.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationRequest.create
import com.picrunner.MainActivity
import com.picrunner.R
import com.picrunner.util.isServiceRunningInForeground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LocationService : LifecycleService() {

    companion object {

        const val KEY_EXTRA_LOCATION = "location"
        const val KEY_EXTRA_SERVICE_STATE = "locationServiceState"
        const val ACTION_LOCATION_BROADCAST = "locationBroadcast"
        const val ACTION_SERVICE_STATE_BROADCAST = "locationServiceStateBroadcast"
        const val TAG = "LocationService"

        private const val NOTIFICATION_CHANNEL_ID = "locationNotificationChannel"
        private const val NOTIFICATION_ID = 1
        private const val KEY_EXTRA_LAUNCHED_FROM_NOTIFICATION = "launchedFromNotification"

        private const val SMALLEST_DISPLACEMENT_IN_METERS = 100F
        private const val INTERVAL_TIME_IN_MILLIS = 20_000L
        private const val FASTEST_INTERVAL_TIME_IN_MILLIS = 10_000L
    }

    private val job = SupervisorJob()
    private val locationServiceScope = CoroutineScope(Dispatchers.IO + job)

    private val binder = LocationBinder()

    private var locationRequest: LocationRequest? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationServiceHandler: Handler? = null
    private var location: Location? = null
    private var notificationManager: NotificationManager? = null
    private var locationCallback: LocationCallback? = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            onNewLocation(locationResult.lastLocation)
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Location service started")
        val startedFromNotification = intent?.getBooleanExtra(
            KEY_EXTRA_LAUNCHED_FROM_NOTIFICATION,
            false
        )

        // If user decides to stop location updates from the notification.
        if (startedFromNotification!!) {
            cancelLocationUpdates()
            stopSelf()
        }
        // For service not to be recreated after killing.
        return super.onStartCommand(intent, flags, START_NOT_STICKY)
    }

    // When activity comes to foreground this method is being called
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.i(TAG, "in onBind()")
        stopForeground(true)
        return binder
    }

    override fun onRebind(intent: Intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground and
        // binds once again with this service.
        // The service should cease to be a foreground service when that happens.
        Log.i(TAG, "in onRebind()")
        stopForeground(true)
        onLocationServiceStateChanged(true)
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.i(TAG, "Unbound from service and starting foreground")
        startForeground(NOTIFICATION_ID, getNotification())
        onLocationServiceStateChanged(false)
        return true
    }

    override fun onDestroy() {
        locationServiceHandler?.removeCallbacksAndMessages(null)
        job.cancel()
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
//            TODO clear collected images list
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
            stopSelf()
            onLocationServiceStateChanged(false)
        } catch (exception: SecurityException) {
            Log.e(TAG,exception.message.toString())
        }
    }

    private fun getLastLocation() {
        try {
            fusedLocationProviderClient?.lastLocation?.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    location = task.result!!
                } else {
                    Log.w(TAG,"Location request failed.")
                }
            }
        } catch (exception: SecurityException) {
            Log.e(TAG,exception.message.toString())
        }
    }

    private fun onLocationServiceStateChanged(isRunning: Boolean) {
        locationServiceScope.launch {
            val intent = Intent(ACTION_SERVICE_STATE_BROADCAST)
            intent.putExtra(KEY_EXTRA_SERVICE_STATE, isRunning)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    /** This function call every time we have a unique location according to
     * location request parameters and send broadcast intent with location
     * put in extras to receive it outside via BroadcastReceiver */
    private fun onNewLocation(location: Location) {
        Log.i(TAG,"New location: $location")
        this.location = location

        locationServiceScope.launch {
            val intent = Intent(ACTION_LOCATION_BROADCAST)
            intent.putExtra(KEY_EXTRA_LOCATION, location)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }

        if (this.isServiceRunningInForeground()) {
            notificationManager?.notify(NOTIFICATION_ID, getNotification())
        }
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