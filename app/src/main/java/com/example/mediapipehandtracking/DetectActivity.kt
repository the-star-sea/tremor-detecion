package com.example.mediapipehandtracking

import android.graphics.PointF
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mediapipehandtracking.remote.ApiService
import com.example.mediapipehandtracking.remote.Network
import com.example.mediapipehandtracking.responseClasses.ResponseClass
import com.example.mediapipehandtracking.responseClasses.ResponseRegisterClass
import com.example.mediapipehandtracking.responseClasses.ResponseSignalClass
import com.example.mediapipehandtracking.responseClasses.ResponseSignalIdClass
import com.google.mediapipe.components.CameraHelper.CameraFacing
import com.google.mediapipe.components.CameraXPreviewHelper
import com.google.mediapipe.components.ExternalTextureConverter
import com.google.mediapipe.components.FrameProcessor
import com.google.mediapipe.components.PermissionHelper
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList
import com.google.mediapipe.framework.AndroidAssetUtil
import com.google.mediapipe.framework.Packet
import com.google.mediapipe.framework.PacketGetter
import com.google.mediapipe.glutil.EglManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetectActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val BINARY_GRAPH_NAME = "multihandtrackinggpu.binarypb"
    private val INPUT_VIDEO_STREAM_NAME = "input_video"
    private val OUTPUT_VIDEO_STREAM_NAME = "output_video"
    private val OUTPUT_LANDMARKS_STREAM_NAME = "multi_hand_landmarks"
    private val CAMERA_FACING = CameraFacing.FRONT
    // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
    // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
    // This is needed because OpenGL represents images assuming the image origin is at the
    // bottom-left corner, whereas MediaPipe in general assumes the image origin is at top-left.
    private val FLIP_FRAMES_VERTICALLY = true

    companion object {
        init {
            // Load all native libraries needed by the app.
            System.loadLibrary("mediapipe_jni")
            System.loadLibrary("opencv_java3")
        }
    }

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private var previewFrameTexture: SurfaceTexture? = null
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private var previewDisplayView: SurfaceView? = null
    // Creates and manages an {@link EGLContext}.
    private var eglManager: EglManager? = null
    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    private var processor: FrameProcessor? = null
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private var converter: ExternalTextureConverter? = null
    // Handles camera access via the {@link CameraX} Jetpack support library.
    private var cameraHelper: CameraXPreviewHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previewDisplayView = SurfaceView(this)
        setupPreviewDisplayView()
        // Initialize asset manager so that MediaPipe native libraries can access the app assets,
        // e.g., binary graphs.
        AndroidAssetUtil.initializeNativeAssetManager(this)
        eglManager = EglManager(null)
        processor = FrameProcessor(
            this,
            eglManager!!.nativeContext,
            BINARY_GRAPH_NAME,
            INPUT_VIDEO_STREAM_NAME,
            OUTPUT_VIDEO_STREAM_NAME
        )
        processor!!.videoSurfaceOutput.setFlipY(FLIP_FRAMES_VERTICALLY)
        processor!!.addPacketCallback(
            OUTPUT_LANDMARKS_STREAM_NAME
        ) { packet: Packet ->
            Log.d(TAG, "Received multi-hand landmarks packet.")
            val multiHandLandmarks =
                PacketGetter.getProtoVector(
                    packet,
                    NormalizedLandmarkList.parser()
                )
            Log.d(
                TAG,
                "[TS:"
                        + packet.timestamp
                        + "] "
                        + getMultiHandLandmarksDebugString(multiHandLandmarks)
            )

            var sft = sft(multiHandLandmarks)
            var shm = shm(multiHandLandmarks)
            var sps = sps(multiHandLandmarks)

            val apiService: ApiService = Network.getInstance().create(ApiService::class.java)
            val responseSignalClass = ResponseSignalClass(
                sft,shm,sps,packet.timestamp
            )
            apiService.save(responseSignalClass)
                .enqueue(object : Callback<ResponseSignalIdClass> {
                    override fun onResponse(
                        call: Call<ResponseSignalIdClass?>,
                        response: Response<ResponseSignalIdClass?>
                    ) {
                        if (response.body() != null) {
                            // ...
                        } else {
                            Toast.makeText(
                                this@DetectActivity,
                                "something went wrong! please try again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseSignalIdClass?>, t: Throwable) {}
                })
        }
        PermissionHelper.checkAndRequestCameraPermissions(this)
    }

    override fun onResume() {
        super.onResume()
        converter = ExternalTextureConverter(eglManager!!.context)
        converter!!.setFlipY(FLIP_FRAMES_VERTICALLY)
        converter!!.setConsumer(processor)
        if (PermissionHelper.cameraPermissionsGranted(this)) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        converter!!.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupPreviewDisplayView() {
        previewDisplayView!!.visibility = View.GONE
        val viewGroup = findViewById<ViewGroup>(R.id.preview_display_layout)
        viewGroup.addView(previewDisplayView)
        previewDisplayView!!
            .getHolder()
            .addCallback(
                object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        processor!!.videoSurfaceOutput.setSurface(holder.surface)
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        // (Re-)Compute the ideal size of the camera-preview display (the area that
                        // the camera-preview frames get rendered onto, potentially with scaling and
                        // rotation) based on the size of the SurfaceView that contains the display.
                        val viewSize = Size(width, height)
                        val displaySize = cameraHelper!!.computeDisplaySizeFromViewSize(viewSize)
                        // Connect the converter to the camera-preview frames as its input (via
                        // previewFrameTexture), and configure the output width and height as the
                        // computed display size.
                        converter!!.setSurfaceTextureAndAttachToGLContext(
                            previewFrameTexture, displaySize.width, displaySize.height
                        )
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        processor!!.videoSurfaceOutput.setSurface(null)
                    }
                })
    }

    private fun startCamera() {
        cameraHelper = CameraXPreviewHelper()
        cameraHelper!!.setOnCameraStartedListener { surfaceTexture: SurfaceTexture? ->
            previewFrameTexture = surfaceTexture
            // Make the display view visible to start showing the preview. This triggers the
            // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
            previewDisplayView!!.visibility = View.VISIBLE
        }
        cameraHelper!!.startCamera(this, CAMERA_FACING,  /*surfaceTexture=*/null)
    }

    private fun getMultiHandLandmarksDebugString(
        multiHandLandmarks: List<NormalizedLandmarkList>
    ): String {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand landmarks"
        }
        var multiHandLandmarksStr =
            "Number of hands detected: " + multiHandLandmarks.size + "\n"
        for ((handIndex, landmarks) in multiHandLandmarks.withIndex()) {
            multiHandLandmarksStr +=
                "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.landmarkCount + "\n"
            for ((landmarkIndex, landmark) in landmarks.landmarkList.withIndex()) {
                multiHandLandmarksStr += ("\t\tLandmark ["
                        + landmarkIndex
                        + "]: ("
                        + landmark.x
                        + ", "
                        + landmark.y
                        + ", "
                        + landmark.z
                        + ")\n")
            }
        }
        return multiHandLandmarksStr
    }
    private fun sft( multiHandLandmarks: List<NormalizedLandmarkList>
    ):Double{
        if (multiHandLandmarks.isEmpty()) {
            return -1.0
        }
        if (multiHandLandmarks.size>1) {
            return -2.0
        }
        var v1=multiHandLandmarks.get(0).getLandmarkList().get(4).getX()
        var v2=multiHandLandmarks.get(0).getLandmarkList().get(8).getX()

        var v3=multiHandLandmarks.get(0).getLandmarkList().get(4).getY()
        var v4=multiHandLandmarks.get(0).getLandmarkList().get(8).getY()

        var v5=multiHandLandmarks.get(0).getLandmarkList().get(4).getZ()
        var v6=multiHandLandmarks.get(0).getLandmarkList().get(8).getZ()

        return Math.sqrt(((v1-v2)*(v1-v2)+(v3-v4)*(v3-v4)+(v5-v6)*(v5-v6)).toDouble())
    }

    private fun xmulti(p1: PointF, p2: PointF, p0: PointF): Float
    {
        return (p1.x - p0.x) * (p2.y - p0.y) - (p2.x - p0.x) * (p1.y - p0.y)
    }

    fun getS(a: PointF, b: PointF, c: PointF): Float
    {
        return ((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)) / 2
    }

    fun getPS(p: Array<PointF>, pl: IntArray, n: Int): Float
    {
        var sumS = 0.0F
        for (i in 2..n - 1) sumS += Math.abs(getS(p[pl[1]], p[pl[i]], p[pl[i + 1]]))
        return sumS
    }

    private fun graham(point: Array<PointF>,n: Int, pl: IntArray): Int{
        var i:Int
        var t:Int = 1
        for(i in 1..n){
            if(point[i].y<point[t].y){
                t=i
            }
        }

        pl[1]=t
        var num:Int = 1

        do {
            num++;
            t = pl[num-1]+1;
            if (t > n) t = 1
            for (i in 1..n) {
                val x: Float = xmulti(point.get(i), point.get(t), point.get(pl[num - 1]))
                if (x < 0)
                    t = i
            }
            pl[num] = t
        }while(pl[num]!=pl[1])
        return num-1
    }

    private fun shm( multiHandLandmarks: List<NormalizedLandmarkList>
    ):Double{
        if (multiHandLandmarks.isEmpty()) {
            return -1.0
        }
        if (multiHandLandmarks.size>1) {
            return -2.0
        }

        var point = Array(22,{PointF(0F,0F)})
        var pl = IntArray(22)

        for (index in 1..21){
            point[index]= PointF(multiHandLandmarks[0].getLandmarkList()[index-1].getX(),multiHandLandmarks[0].getLandmarkList()[index-1].getY())
        }

        var num: Int = graham(point, 21, pl)

        return getPS(point,pl,num).toDouble()
    }

    fun med(list: List<Float>) = list.sorted().let {
        (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
    }
    private fun sps( multiHandLandmarks: List<NormalizedLandmarkList>
    ): Double {
        if (multiHandLandmarks.isEmpty()) {
            return -1.0
        }
        if (multiHandLandmarks.size>1) {
            return -2.0
        }
        var v1=multiHandLandmarks.get(0).getLandmarkList().get(1).getX()
        var v2=multiHandLandmarks.get(0).getLandmarkList().get(2).getX()
        var v3=multiHandLandmarks.get(0).getLandmarkList().get(3).getX()
        var v4=multiHandLandmarks.get(0).getLandmarkList().get(4).getX()
        var med1= med(listOf(v1,v2,v3,v4))
        var v17=multiHandLandmarks.get(0).getLandmarkList().get(17).getX()
        var v18=multiHandLandmarks.get(0).getLandmarkList().get(18).getX()
        var v19=multiHandLandmarks.get(0).getLandmarkList().get(19).getX()
        var v20=multiHandLandmarks.get(0).getLandmarkList().get(20).getX()
        var med2= med(listOf(v17,v18,v19,v20))
        return (med1-med2).toDouble()
    }
}
