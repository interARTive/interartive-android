package com.github.zakaprov.interartive.domain

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.github.zakaprov.interartive.R
import com.github.zakaprov.interartive.extensions.isPermissionGranted
import com.github.zakaprov.interartive.graphics.AugmentedImageRenderer
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 127
    }

    private val arCoreSession = Session(this)
    private val imageDb = AugmentedImageDatabase(arCoreSession)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestCameraPermission()
    }

    override fun onPause() {
        super.onPause()
        arCoreSession.pause()
    }

    override fun onResume() {
        super.onResume()
        arCoreSession.resume()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED) {
                setUpArCore()
            } else {
                finish()
            }
        }
    }

    private fun requestCameraPermission() {
        if (!isPermissionGranted(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST)
        }
    }

    private fun setUpArCore() {
        val config = Config(arCoreSession)
        config.augmentedImageDatabase = imageDb
        config.planeFindingMode = Config.PlaneFindingMode.VERTICAL

        arCoreSession.configure(config)

        main_surface_view.setRenderer(AugmentedImageRenderer(arCoreSession))
    }
}
