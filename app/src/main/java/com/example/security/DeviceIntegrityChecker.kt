package com.example.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import java.io.File

object DeviceIntegrityChecker {
    fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun isCompromised(context: Context): Boolean {
        // We only check for root to avoid breaking the debug emulator environment in AI Studio
        // However, the prompt specifically asks to detect if the device is rooted OR if debuggable mode is active.
        // So we will implement both.
        return isDeviceRooted() || isDebuggable(context)
    }
}
