package com.tsu.vkkfilteringapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.tsu.vkkfilteringapp.databinding.ActivityImageBinding
import com.tsu.vkkfilteringapp.filters.UnsharpMasking
import java.io.File


class ImageActivity : AppCompatActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val FILE_NAME = "photo.jpg"
    private val REQUEST_CODE = 912392355
    private lateinit var photoFile : File
    private lateinit var binding: ActivityImageBinding
    private val GALLERY_REQ_CODE =82949851
    private var photoBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        registerPermission()
        requestPermission()

        binding.camera.setOnClickListener {
            takePhoto(view)
        }
        binding.gallery.setOnClickListener{
            getPhoto(view)
        }
        binding.mask.setOnClickListener{
            blurPhoto(view)
        }
    }

    private fun blurPhoto(view: View) {
        if (photoBitmap != null) {
            val blur = UnsharpMasking(photoBitmap, 1.0,5)
            photoBitmap = blur.newImg
            binding.twoFish.setImageBitmap(photoBitmap)

            Toast.makeText(this,"processed",Toast.LENGTH_SHORT).show()
        }
        else
            Toast.makeText(this,"u not use photo",Toast.LENGTH_SHORT).show()

    }

    private fun getPhoto(view: View) {
         val iGallery: Intent= Intent(Intent.ACTION_PICK);
        iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(iGallery, GALLERY_REQ_CODE)
    }


    private fun registerPermission() {

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    Log.i("Permission: ", "Granted")
                    Toast.makeText(this, "Camera work", Toast.LENGTH_LONG).show()
                    //Log.d("start","when start")
                } else {
                    Log.i("Permission: ", "Denied")
                    Toast.makeText(this, "Camera not work", Toast.LENGTH_LONG).show()
                    //requestPermission()
                }
            }
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("Permission: ", "Granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                Toast.makeText(
                    this,
                    "We need your permission to use the camera",
                    Toast.LENGTH_LONG
                ).show();
            }


            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePhoto(view: View) {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile= getPhotoFile(FILE_NAME)
        Log.e("cameraIntent","100")
        val fileProvider = FileProvider.getUriForFile(this,"com.tsu.vkkfilteringapp.fileprovider",photoFile)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileProvider)
        Log.e("cameraIntent","103")

        if(takePhotoIntent.resolveActivity(this.packageManager)!=null){
            Log.e("cameraIntent","107")

            startActivityForResult(takePhotoIntent,REQUEST_CODE)
        }else{
            Toast.makeText(this,"camera not work",Toast.LENGTH_SHORT).show()
        }

    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName,".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode ==REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Log.e("cameraIntent","122")

            photoBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

            val imageView = binding.twoFish


            imageView.setImageBitmap(photoBitmap)
            //imageView.setImageBitmap(bitmap)

            Toast.makeText(this, " все ок2", Toast.LENGTH_LONG).show()
        }
        else if(requestCode ==GALLERY_REQ_CODE && resultCode == Activity.RESULT_OK){
            val imageView = binding.twoFish
            if (data != null) {
                photoBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,data.data)
                imageView.setImageURI(data.data)
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
