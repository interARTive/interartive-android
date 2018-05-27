package com.github.zakaprov.interartive.renderers

import android.content.ContentValues.TAG
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.github.zakaprov.interartive.domain.SurfaceClickListener
import com.github.zakaprov.interartive.utils.DisplayRotationHelper
import com.github.zakaprov.interartive.utils.SurfaceTapHelper
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import java.io.IOException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainRenderer(
    private val context: Context,
    private val session: Session,
    private val surfaceListener: SurfaceClickListener,
    private val tapHelper: SurfaceTapHelper,
    private val rotationHelper: DisplayRotationHelper
) : GLSurfaceView.Renderer {

    private val backgroundRenderer = BackgroundRenderer()
    private val pointCloudRenderer = PointCloudRenderer()
    private val planeRenderer = PlaneRenderer()
    private val virtualObject = ObjectRenderer()
    private val virtualObjectShadow = ObjectRenderer()

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        try {
            backgroundRenderer.createOnGlThread(context)
            planeRenderer.createOnGlThread(context, "models/trigrid.png")
            pointCloudRenderer.createOnGlThread(context)

            virtualObject.createOnGlThread(context, "models/andy.obj", "models/andy.png")
            virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f)

            virtualObjectShadow.createOnGlThread(
                context, "models/andy_shadow.obj", "models/andy_shadow.png")
            virtualObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow)
            virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f)

        } catch (e: IOException) {
            Log.e(TAG, "Failed to read an asset file", e)
        }

    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        rotationHelper.onSurfaceChanged(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        rotationHelper.updateSessionIfNeeded(session)

        try {
            session.setCameraTextureName(backgroundRenderer.textureId)

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            val frame = session.update()
            val camera = frame.camera

            // Handle taps. Handling only one tap per frame, as taps are usually low frequency
            // compared to frame rate.
            val tap = tapHelper.poll()
            if (tap != null && camera.trackingState == TrackingState.TRACKING) {
                surfaceListener.onSurfaceClicked(frame.hitTest(tap))
//                for (hit in frame.hitTest(tap)) {
//                    // Check if any plane was hit, and if it was hit inside the plane polygon
//                    val trackable = hit.trackable
//                    // Creates an anchor if a plane or an oriented point was hit.
//                    if ((trackable is Plane
//                            && trackable.isPoseInPolygon(hit.hitPose)
//                            && PlaneRenderer.calculateDistanceToPlane(hit.hitPose, camera.pose) > 0) || trackable is Point && trackable.orientationMode == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL) {
//                        // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
//                        // Cap the number of objects created. This avoids overloading both the
//                        // rendering system and ARCore.
//                        if (anchors.size >= 20) {
//                            anchors.get(0).detach()
//                            anchors.removeAt(0)
//                        }
//                        // Adding an Anchor tells ARCore that it should track this position in
//                        // space. This anchor is created on the Plane to place the 3D model
//                        // in the correct position relative both to the world and to the plane.
//                        anchors.add(hit.createAnchor())
//                        break
//                    }
//                }
            }

            // Draw background.
            backgroundRenderer.draw(frame)

            // If not tracking, don't draw 3d objects.
            if (camera.trackingState == TrackingState.PAUSED) {
                return
            }

            // Get projection matrix.
            val projmtx = FloatArray(16)
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f)

            // Get camera matrix and draw.
            val viewmtx = FloatArray(16)
            camera.getViewMatrix(viewmtx, 0)

            // Compute lighting from average intensity of the image.
            // The first three components are color scaling factors.
            // The last one is the average pixel intensity in gamma space.
            val colorCorrectionRgba = FloatArray(4)
            frame.lightEstimate.getColorCorrection(colorCorrectionRgba, 0)

            // Visualize tracked points.
            val pointCloud = frame.acquirePointCloud()
            pointCloudRenderer.update(pointCloud)
            pointCloudRenderer.draw(viewmtx, projmtx)

            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloud.release()

            // Visualize planes.
            planeRenderer.drawPlanes(
                session.getAllTrackables(Plane::class.java), camera.displayOrientedPose, projmtx)

            // Visualize anchors created by touch.
//            val scaleFactor = 1.0f
//            for (anchor in anchors) {
//                if (anchor.getTrackingState() != TrackingState.TRACKING) {
//                    continue
//                }
//                // Get the current pose of an Anchor in world space. The Anchor pose is updated
//                // during calls to session.update() as ARCore refines its estimate of the world.
//                anchor.getPose().toMatrix(anchorMatrix, 0)
//
//                // Update and draw the model and its shadow.
//                virtualObject.updateModelMatrix(anchorMatrix, scaleFactor)
//                virtualObjectShadow.updateModelMatrix(anchorMatrix, scaleFactor)
//                virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba)
//                virtualObjectShadow.draw(viewmtx, projmtx, colorCorrectionRgba)
//            }

        } catch (t: Throwable) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t)
        }

    }
}