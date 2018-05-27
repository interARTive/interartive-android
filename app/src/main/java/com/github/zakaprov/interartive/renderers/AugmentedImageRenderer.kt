package com.github.zakaprov.interartive.renderers

import android.opengl.GLSurfaceView
import android.util.Log
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Session
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AugmentedImageRenderer(private val session: Session) : GLSurfaceView.Renderer {

    override fun onDrawFrame(gl: GL10) {
        val frame = session.update()
        val updatedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)

        for (image in updatedImages) {
            Log.d(javaClass.simpleName, "${image.trackingState.name} ${image.index}")
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) { }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) { }
}