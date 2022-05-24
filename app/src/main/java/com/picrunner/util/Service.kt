package com.picrunner.util

import android.app.ActivityManager
import android.app.Service
import android.content.Context

fun Service.isServiceRunningInForeground(): Boolean {
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    manager.getRunningServices(Integer.MAX_VALUE).find {
        this.javaClass.name == it.service.className
    }?.let { return it.foreground }
    return false
}