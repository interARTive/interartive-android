package com.github.zakaprov.interartive.domain

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.github.zakaprov.interartive.R
import com.github.zakaprov.interartive.extensions.isPermissionGranted
import com.github.zakaprov.interartive.renderers.MainRenderer
import com.github.zakaprov.interartive.utils.DisplayRotationHelper
import com.github.zakaprov.interartive.utils.SurfaceTapHelper
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ArCoreSessionListener {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 127
    }

    private val arCoreSession by lazy { Session(this) }
    private val rotationHelper by lazy { DisplayRotationHelper(this) }
    private val imageDb by lazy {
        val inputStream = assets.open("reference.imgdb")
        AugmentedImageDatabase.deserialize(arCoreSession, inputStream)
    }
    private val tapHelper by lazy { SurfaceTapHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_surface_view.setOnTouchListener(tapHelper)

//        requestCameraPermission()

        setUpArCore()
    }

    override fun onPause() {
        super.onPause()
        stopRendering()
    }

    override fun onResume() {
        super.onResume()

        if (isPermissionGranted(Manifest.permission.CAMERA)) {
            resumeRendering()
        }
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

    override fun onSurfaceClicked(results: List<HitResult>) {
        for (hit in results) {
            // Check if any plane was hit, and if it was hit inside the plane polygon
            val trackable = hit.trackable
            if (trackable is AugmentedImage) {
                Log.d(javaClass.simpleName, "Clicked on: ${trackable.name}")
            }
        }
    }

    override fun onAugmentedImagesFound(images: Collection<AugmentedImage>) {
        for (image in images) {
            if (image.trackingState == TrackingState.TRACKING) {
                Log.d(javaClass.simpleName, "TRACKING: ${image.name} ${image.centerPose}")
            }
        }
    }

    private fun resumeRendering() {
        try {
            arCoreSession.resume()
        } catch (exception: CameraNotAvailableException) {
            Toast.makeText(this, "Camera not available, closing app.", Toast.LENGTH_LONG).show()
            finish()
        }

        main_surface_view.onResume()
        rotationHelper.onResume()
    }

    private fun stopRendering() {
        rotationHelper.onPause()
        main_surface_view.onPause()
        arCoreSession.pause()
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
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL

        arCoreSession.configure(config)
        setUpRenderer()

        resumeRendering()
    }

    private fun setUpRenderer() {
        main_surface_view.preserveEGLContextOnPause = true
        main_surface_view.setEGLContextClientVersion(2)
        main_surface_view.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        main_surface_view.setRenderer(MainRenderer(this, arCoreSession, this, tapHelper, rotationHelper))
        main_surface_view.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}
