package com.github.zakaprov.interartive.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

inline fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
