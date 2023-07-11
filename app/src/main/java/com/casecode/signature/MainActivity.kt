package com.casecode.signature

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.gcacace.signaturepad.views.SignaturePad
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import com.squareup.picasso.Picasso
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import android.Manifest
import android.annotation.SuppressLint
import android.widget.ImageView
import java.io.FileInputStream


class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100

    private fun checkWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your task
                savePdfToPublicDirectory()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // For devices running below Android M, no runtime permission is required
            savePdfToPublicDirectory()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your task
                savePdfToPublicDirectory()
            } else {
                val imageUrl =
                    "http://10.2.1.220:8080/HyperOneBusiness/api_development_v1_prod/login/signature/35389.gif"

                downloadImageAndProcess(imageUrl)
                // Permission denied, handle it accordingly (e.g., show an error message)
            }
        }
    }

    private fun savePdfToPublicDirectory() {
        val imageUrl =
            "http://10.2.1.220:8080/HyperOneBusiness/api_development_v1_prod/login/signature/35389.gif"

        downloadImageAndProcess(imageUrl)

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mSignaturePad = findViewById<SignaturePad>(R.id.signature_pad)
        val startSigningButton = findViewById<Button>(R.id.btn_start_signing)
        val signedButton = findViewById<Button>(R.id.btn_signed)
        val clearButton = findViewById<Button>(R.id.btn_clear)
        val image = findViewById<ImageView>(R.id.imageView2)


        mSignaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                //Event triggered when the pad is touched
            }

            override fun onSigned() {
                //Event triggered when the pad is signed
            }

            override fun onClear() {
                //Event triggered when the pad is cleared
            }
        })

        clearButton.setOnClickListener {
            mSignaturePad.clear()
        }

        signedButton.setOnClickListener {
            val signatureBitmap = mSignaturePad.signatureBitmap
            val transparentSignatureBitmap = mSignaturePad.transparentSignatureBitmap
            val signatureSvg = mSignaturePad.signatureSvg

            //addImageInPdfNew(transparentSignatureBitmap)
        }

        // Check if the bitmap is not null before using it

        startSigningButton.setOnClickListener {
            checkWriteStoragePermission()
        }
    }

    private fun downloadImageAndProcess(imageUrl: String ) {
        Picasso.get().load(imageUrl).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
                    val bitmaps = listOf(resizedBitmap, resizedBitmap, resizedBitmap)
                    val positions = listOf(
                        Pair(20f, 5f),   // Position for image1
                        Pair(250f, 5f),  // Position for image2
                        Pair(470f, 5f)  // Position for image3
                    )

                    addImageInPdf(bitmaps,positions)


                } else {
                    Toast.makeText(this@MainActivity, "Failed to download image", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                Toast.makeText(this@MainActivity, "Failed to download image", Toast.LENGTH_SHORT).show()
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                // Do nothing
            }
        })
    }


}


private fun addImageInPdf(bitmaps: List<Bitmap>, positions: List<Pair<Float, Float>>){
    // Add image to PDF file
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val outputFile = File(downloadsDir, "HyperOne.pdf")
    val outputStream =
        FileOutputStream(File(downloadsDir, "modified.pdf")) // Specify the output file name or path
    val reader = PdfReader(outputFile.inputStream())
    val stamper = PdfStamper(reader, outputStream)
    for (index in 1..reader.numberOfPages) {
        for (i in bitmaps.indices){
            val stream = ByteArrayOutputStream()
            bitmaps[i].compress(Bitmap.CompressFormat.PNG, 100, stream)
            val image = Image.getInstance(stream.toByteArray())
            val (x, y) = positions[i]
            image.setAbsolutePosition(x, y)

            val content = stamper.getOverContent(index)
            content.addImage(image)
        }
    }
    stamper.close()
    reader.close()
    outputStream.close()
}