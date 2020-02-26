package com.rifafauzi.customcamera.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.TextureViewMeteringPointFactory
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.rifafauzi.customcamera.R
import com.rifafauzi.customcamera.utils.FileCreator
import com.rifafauzi.customcamera.utils.FileCreator.JPEG_FORMAT
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.*
import java.util.concurrent.Executors

/**
 * A simple [Fragment] subclass.
 */
class CameraFragment : Fragment() {

    private lateinit var rectangle: View

    private lateinit var processCameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processCameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rectangle = view.findViewById(R.id.rectangle)

        processCameraProviderFuture.addListener(Runnable {
            processCameraProvider = processCameraProviderFuture.get()
            viewFinder.post { setupCamera() }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::processCameraProvider.isInitialized) {
            processCameraProvider.unbindAll()
        }
    }

    private fun setupCamera() {
        processCameraProvider.unbindAll()
        val camera = processCameraProvider.bindToLifecycle(
            this,
            CameraSelector.DEFAULT_BACK_CAMERA,
            buildPreviewUseCase(),
            buildImageCaptureUseCase(),
            buildImageAnalysisUseCase())
        setupTapForFocus(camera.cameraControl)
    }

    private fun buildPreviewUseCase(): Preview {
        val display = viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val preview = Preview.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .build()
            .apply {
                previewSurfaceProvider = viewFinder.previewSurfaceProvider
            }
        preview.previewSurfaceProvider = viewFinder.previewSurfaceProvider
        return preview
    }

    private fun buildImageCaptureUseCase(): ImageCapture {
        val display = viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val capture = ImageCapture.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        val executor = Executors.newSingleThreadExecutor()
        cameraCaptureImageButton.setOnClickListener {
            capture.takePicture(
                FileCreator.createTempFile(JPEG_FORMAT),
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(file: File) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        val rotatedBitmap = bitmap.rotate(90)
                        val croppedImage = cropImage(rotatedBitmap, viewFinder, rectangle)
                        val path = saveImage(croppedImage)
                        requireActivity().runOnUiThread {
                            launchGalleryFragment(path)
                        }
                    }

                    override fun onError(imageCaptureError: Int, message: String, cause: Throwable?) {
                        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
                        Log.e("CameraFragment", "Capture error $imageCaptureError: $message", cause)
                    }
                })
        }
        return capture
    }

    private fun buildImageAnalysisUseCase(): ImageAnalysis {
        val display = viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val analysis = ImageAnalysis.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
            .setImageQueueDepth(10)
            .build()
        analysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            ImageAnalysis.Analyzer { imageProxy ->
                Log.d("CameraFragment", "Image analysis result $imageProxy")
                imageProxy.close()
            })
        return analysis
    }

    private fun setupTapForFocus(cameraControl: CameraControl) {
        viewFinder.setOnTouchListener { _, event ->
            if (event.action != MotionEvent.ACTION_UP) {
                return@setOnTouchListener true
            }

            val textureView = viewFinder.getChildAt(0) as? TextureView
                ?: return@setOnTouchListener true
            val factory = TextureViewMeteringPointFactory(textureView)

            val point = factory.createPoint(event.x, event.y)
            val action = FocusMeteringAction.Builder.from(point).build()
            cameraControl.startFocusAndMetering(action)
            return@setOnTouchListener true
        }
    }

    /**
     * Crop a image taking a reference a view parent like a frame, and a view child like final
     * reference
     *
     * @param bitmap image to crop
     * @param frame where the image is set it
     * @param reference frame to take reference for crop the image
     * @return image already cropped
     */
    private fun cropImage(bitmap: Bitmap, frame: View, reference: View): ByteArray {
        val heightOriginal = frame.height
        val widthOriginal = frame.width
        val heightFrame = reference.height
        val widthFrame = reference.width
        val leftFrame = reference.left
        val topFrame = reference.top
        val heightReal = bitmap.height
        val widthReal = bitmap.width
        val widthFinal = widthFrame * widthReal / widthOriginal
        val heightFinal = heightFrame * heightReal / heightOriginal
        val leftFinal = leftFrame * widthReal / widthOriginal
        val topFinal = topFrame * heightReal / heightOriginal
        val bitmapFinal = Bitmap.createBitmap(
            bitmap,
            leftFinal, topFinal, widthFinal, heightFinal
        )
        val stream = ByteArrayOutputStream()
        bitmapFinal.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            stream
        ) //100 is the best quality possible
        return stream.toByteArray()
    }

    // Save the image cropped
    private fun saveImage(bytes: ByteArray) : String {
        val outStream: FileOutputStream
        val fileName = "KTP" + System.currentTimeMillis() + ".jpg"
        val directoryName = File(Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY)
        val file = File(directoryName, fileName)

        if (!directoryName.exists()) {
            directoryName.mkdirs()
        }

        try {
            file.createNewFile()
            outStream = FileOutputStream(file)
            outStream.write(bytes)
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.path),
                arrayOf("image/jpeg"), null
            )
            outStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.absolutePath
    }


    // Rotate the image from Landscape to Portrait
    private fun Bitmap.rotate(degree:Int):Bitmap{
        // Initialize a new matrix
        val matrix = Matrix()

        // Rotate the bitmap
        matrix.postRotate(degree.toFloat())

        // Resize the bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(
            this,
            width,
            height,
            true
        )

        // Create and return the rotated bitmap
        return Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
    }

    private fun launchGalleryFragment(path: String) {
        val action =
            CameraFragmentDirections.actionLaunchGalleryFragment(
                path
            )
        findNavController().navigate(action)
    }

    companion object {
        private const val IMAGE_DIRECTORY = "/CustomImage"
    }

}
