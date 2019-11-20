package com.rifafauzi.customcamerasurfaceview.ui


import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.navigation.Navigation
import com.rifafauzi.customcamerasurfaceview.R
import com.rifafauzi.customcamerasurfaceview.utils.FileCreator
import com.rifafauzi.customcamerasurfaceview.utils.FileCreator.JPEG_FORMAT
import com.rifafauzi.customcamerasurfaceview.utils.UseCaseConfigBuilder
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.util.concurrent.Executors

/**
 * A simple [Fragment] subclass.
 */
class CameraFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewFinder.post { setupCamera() }
    }

    private fun setupCamera() {
        CameraX.unbindAll()
        CameraX.bindToLifecycle(
            this,
            buildPreviewUseCase(),
            buildImageCaptureUseCase(),
            buildImageAnalysisUseCase()
        )
    }

    private fun buildPreviewUseCase(): Preview {
        val preview = Preview(
            UseCaseConfigBuilder.buildPreviewConfig(
                viewFinder.display
            )
        )
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            updateViewFinderWithPreview(previewOutput)
            correctPreviewOutputForDisplay(previewOutput.textureSize)
        }
        return preview
    }

    private fun updateViewFinderWithPreview(previewOutput: Preview.PreviewOutput) {
        val parent = viewFinder.parent as ViewGroup
        parent.removeView(viewFinder)
        parent.addView(viewFinder, 0)
        viewFinder.surfaceTexture = previewOutput.surfaceTexture
    }

    /**
     * Corrects the camera/preview's output to the display, by scaling
     * up/down and/or rotating the camera/preview's output.
     */
    private fun correctPreviewOutputForDisplay(textureSize: Size) {
        val matrix = Matrix()

        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        val displayRotation = getDisplayRotation()
        val (dx, dy) = getDisplayScalingFactors(textureSize)

        matrix.postRotate(displayRotation, centerX, centerY)
        matrix.preScale(dx, dy, centerX, centerY)

        // Correct preview output to account for display rotation and scaling
        viewFinder.setTransform(matrix)
    }

    private fun getDisplayRotation(): Float {
        val rotationDegrees = when (viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> throw IllegalStateException("Unknown display rotation ${viewFinder.display.rotation}")
        }
        return -rotationDegrees.toFloat()
    }

    private fun getDisplayScalingFactors(textureSize: Size): Pair<Float, Float> {
        val cameraPreviewRation = textureSize.height / textureSize.width.toFloat()
        val scaledWidth: Int
        val scaledHeight: Int
        if (viewFinder.width > viewFinder.height) {
            scaledHeight = viewFinder.width
            scaledWidth = (viewFinder.width * cameraPreviewRation).toInt()
        } else {
            scaledHeight = viewFinder.height
            scaledWidth = (viewFinder.height * cameraPreviewRation).toInt()
        }
        val dx = scaledWidth / viewFinder.width.toFloat()
        val dy = scaledHeight / viewFinder.height.toFloat()
        return Pair(dx, dy)
    }

    private fun buildImageCaptureUseCase(): ImageCapture {
        val capture = ImageCapture(
            UseCaseConfigBuilder.buildImageCaptureConfig(
                viewFinder.display
            )
        )
        cameraCaptureImageButton.setOnClickListener {
            capture.takePicture(
                FileCreator.createTempFile(JPEG_FORMAT),
                Executors.newSingleThreadExecutor(),
                object : ImageCapture.OnImageSavedListener {
                    override fun onImageSaved(file: File) {
                        val arguments =
                            GalleryFragment.arguments(
                                file.absolutePath
                            )
                        Navigation.findNavController(requireActivity(), R.id.mainContent)
                            .navigate(R.id.galleryFragment, arguments)
                    }

                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        cause: Throwable?
                    ) {
                        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG)
                            .show()
                        Log.e("CameraFragment", "Capture error $imageCaptureError: $message", cause)
                    }
                })
        }
        return capture
    }

    private fun buildImageAnalysisUseCase(): ImageAnalysis {
        val analysis = ImageAnalysis(
            UseCaseConfigBuilder.buildImageAnalysisConfig(
                viewFinder.display
            )
        )
        analysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            ImageAnalysis.Analyzer { image, rotationDegrees ->
                Log.d(
                    "CameraFragment",
                    "Image analysis: $image - Rotation degrees: $rotationDegrees"
                )
            })
        return analysis
    }

}
