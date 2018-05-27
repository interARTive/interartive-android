package com.github.zakaprov.interartive.extensions

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

fun FragmentActivity.getTopStackFragment(): Fragment? {
    val manager = supportFragmentManager

    if (manager.backStackEntryCount > 0) {
        val index = manager.backStackEntryCount - 1
        val entry = manager.getBackStackEntryAt(index)
        return manager.findFragmentByTag(entry.name)
    }

    return null
}