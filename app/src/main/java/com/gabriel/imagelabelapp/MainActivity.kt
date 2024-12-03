package com.gabriel.imagelabelapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gabriel.imagelabelapp.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageLabeler: ImageLabeler


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val objectImage = binding.objectImage
        val btnCamera = binding.btnCamera
        checkCameraPermission()


        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val extras = result.data?.extras
                    val imageBitmap = extras?.getParcelable("data", Bitmap::class.java)
                    if (imageBitmap != null) {
                        objectImage.setImageBitmap(imageBitmap)
                        labelImage(imageBitmap)
                    } else {
                        binding.resultText.text = getString(R.string.unable_to_capture_image)
                    }
                }
            }

        btnCamera.setOnClickListener {
            val clickPicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (clickPicture.resolveActivity(packageManager) != null) {
                cameraLauncher.launch(clickPicture)
            }
        }
    }

    private fun labelImage(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        imageLabeler.process(inputImage).addOnSuccessListener { label ->
            displayLabel(label)
        }.addOnFailureListener { e ->
            binding.resultText.text = "Error: ${e.message}"
        }
    }

    private fun displayLabel(labels: List<ImageLabel>) {
        if (labels.isNotEmpty()) {
            val mostConfidentLabel = labels[0]
            binding.resultText.text = "${mostConfidentLabel.text}"
        } else {
            binding.resultText.text = getString(R.string.no_labels_found)
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
        }
    }
}