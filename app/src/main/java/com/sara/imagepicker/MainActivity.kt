package com.sara.imagepicker

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import java.io.File
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var select_image: ImageView
    private val REQUEST_CODE = 13
    private lateinit var filePhoto: File
    private val FILE_NAME = "photo.jpg"
    private var imageData: ByteArray? = null
    private val IMAGE_CHOOSE = 1000
    private val PERMISSION_CODE = 1001
    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pick_gallery: Button = this.findViewById(R.id.pick_gallery)
        val camera_pic: Button = this.findViewById(R.id.camera_pic)
        val camera_video: Button = this.findViewById(R.id.camera_video)
        val camera_multi: Button = this.findViewById(R.id.camera_multi)
        val btn_share: Button = this.findViewById(R.id.btn_share)
        select_image = this.findViewById(R.id.select_image)

        pick_gallery.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_DENIED){
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, REQUEST_CODE)
                } else{
                    chooseImageGallery()
                }
            }else{
                chooseImageGallery()
            }
        }

        camera_pic.setOnClickListener { cameraButtonClick(::openCameraPicture) }
        camera_video.setOnClickListener { cameraButtonClick(::openCameraVideo) }
        camera_multi.setOnClickListener { cameraButtonClick(::openCameraPopup) }
        btn_share.setOnClickListener {
            if (checkWritePermission()){
                shareToSocialMedia()
            } else {
                requestWritePermission()
            }
        }
    }

    fun cameraButtonClick(function: (() -> (Unit))){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(Manifest.permission.CAMERA)
                requestPermissions(permissions, REQUEST_CODE)
            } else {
                function()
            }
        } else {
            function()
        }
    }

    fun openCameraPicture(){
        var takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        filePhoto = getPhotoFile(FILE_NAME)

        val providerFile =
            FileProvider.getUriForFile(this,"com.sara.imagepicker.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (takePhotoIntent.resolveActivity(this.packageManager) != null){
            startActivityForResult(takePhotoIntent, REQUEST_CODE)
        }else {
        }
    }

    fun openCameraVideo(){
        var takePhotoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        filePhoto = getPhotoFile(FILE_NAME)

        val providerFile =
            FileProvider.getUriForFile(this,"com.sara.imagepicker.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (takePhotoIntent.resolveActivity(this.packageManager) != null){
            startActivityForResult(takePhotoIntent, REQUEST_CODE)
        }else {
        }
    }


    fun openCameraPopup(){
        //var takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        val chooserIntent =
            Intent.createChooser(takePictureIntent, "Capture Image or Video")

        filePhoto = getPhotoFile(FILE_NAME)

        val providerFile =
            FileProvider.getUriForFile(this,"com.innoblock.nuk_cafe.fileprovider", filePhoto)
        //takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)

        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an action")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takeVideoIntent))
        if (chooserIntent.resolveActivity(this.packageManager) != null){
            startActivityForResult(chooserIntent, REQUEST_CODE)
        }else {
        }
    }

    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*,video/*"//"image/*"
        startActivityForResult(intent, IMAGE_CHOOSE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    chooseImageGallery()
                }else{
                    chooseImageGallery()
                    //Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val takenPhoto = BitmapFactory.decodeFile(filePhoto.absolutePath)
            select_image.setImageBitmap(takenPhoto)
            //createImageData(filePhoto)
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
            val uri = data?.data
            select_image.setImageURI(uri)
            //createImageData(uri!!)
            Glide.with(this)
                .asBitmap()
                .load(uri) // or URI/path
                .into(select_image)
        }
    }

    private fun checkWritePermission(): Boolean {
        val result: Int = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this as Activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Toast.makeText(
                this,
                "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this as Activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    fun shareToSocialMedia(){
        val bitmap = (select_image.getDrawable() as BitmapDrawable).getBitmap()
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/jpeg"

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "title")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        val uri = this.contentResolver?.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )


        val outstream: OutputStream?
        try {
            outstream = this?.contentResolver?.openOutputStream(uri!!)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream)
            outstream?.close()
        } catch (e: Exception) {
            System.err.println(e.toString())
        }

        share.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(share, "Share Image"))
    }
}