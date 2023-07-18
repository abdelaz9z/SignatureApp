package com.casecode.signature

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.casecode.signature.model.SignatureResponse
import com.casecode.signature.network.client.RetrofitClient
import com.github.gcacace.signaturepad.views.SignaturePad
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100
    val imageUrlMap = mutableMapOf<String, String>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mSignaturePad = findViewById<SignaturePad>(R.id.signature_pad)
        val startSigningButton = findViewById<Button>(R.id.btn_start_signing)
        val signedButton = findViewById<Button>(R.id.btn_signed)
        val clearButton = findViewById<Button>(R.id.btn_clear)


        imageUrlMap[Constants.STORE_KEY] = Constants.IMAGE_URL
        imageUrlMap[Constants.WORKER_KEY] = Constants.IMAGE_URL
        imageUrlMap[Constants.KEEPER_KEY] = Constants.IMAGE_URL

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


        getSignature("123")
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
                downloadImageAndProcess(imageUrlMap)
            } else {
                downloadImageAndProcess(imageUrlMap)
                // Permission denied, handle it accordingly (e.g., show an error message)
            }
        }
    }

    private fun receivingSignature(orderNumber: String, bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        //convert bitmap to byteArray
        val receivingSignature = outputStream.toByteArray()
        //convert byteArray to string
        val binaryString = Base64.encodeToString(receivingSignature, Base64.DEFAULT)
        //post image on DB
        RetrofitClient.hyperoneApiService().receivingSignature(orderNumber, binaryString)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()

                        // Handle the response

                        Log.i("TAG", "status: $responseBody")
                    } else {
                        // Handle the error
                        Log.d("TAG", "onResponse() returned: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Handle the error
                    Log.d("TAG", "Handle the error: ${t.message}")
                }
            })
    }

    private fun storekeeperSignature(orderNumber: String, bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val storekeeperSignatureByteArray = outputStream.toByteArray()

        val binaryString = Base64.encodeToString(storekeeperSignatureByteArray, Base64.DEFAULT)


        RetrofitClient.hyperoneApiService().storekeeperSignature(orderNumber, binaryString)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()

                        // Handle the response

                        Log.i("TAG", "status: $responseBody")
                    } else {
                        // Handle the error
                        Log.d("TAG", "onResponse() returned: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Handle the error
                    Log.d("TAG", "Handle the error: ${t.message}")
                }
            })
    }

    private fun downloadImageAndProcess(imageUrl: Map<String, String>) {
        val bitmaps = mutableMapOf<String, Bitmap>()

        val target = object : com.squareup.picasso.Target {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                if (bitmap != null) {
                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
                    for ((key, value) in imageUrl) {
                        if (value == imageUrl[key]) {
                            bitmaps[key] = resizedBitmap
                        }
                    }

                    Log.i("TAG", "onBitmapLoaded: $resizedBitmap")

                    if (bitmaps.size == imageUrl.size) {
                        val positions = listOf(
                            Pair(20f, 5f),   // Position for "KEEPER_KEY"
                            Pair(250f, 5f),  // Position for "WORKER_KEY"
                            Pair(470f, 5f)   // Position for "STORE_KEY"
                        )


                        receivingSignature("123", resizedBitmap)
//                        storekeeperSignature("123", resizedBitmap)

                        addImageInPdf(bitmaps, positions)
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to download image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                Toast.makeText(this@MainActivity, "Failed to download image", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                // Do nothing
            }
        }

        for ((_, imageUrlValue) in imageUrl) {
            Picasso.get().load(imageUrlValue).into(target)
        }
    }

    private fun checkWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your task
                downloadImageAndProcess(imageUrlMap)
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // For devices running below Android M, no runtime permission is required
            downloadImageAndProcess(imageUrlMap)
        }
    }

    private fun addImageInPdf(bitmaps: Map<String, Bitmap>, positions: List<Pair<Float, Float>>) {
        // Add image to PDF file
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(downloadsDir, "HyperOne.pdf")
        val outputStream =
            FileOutputStream(
                File(
                    downloadsDir,
                    "modified.pdf"
                )
            ) // Specify the output file name or path
        val reader = PdfReader(outputFile.inputStream())
        val stamper = PdfStamper(reader, outputStream)

        for (index in 1..reader.numberOfPages) {
            for ((key, bitmap) in bitmaps) {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val image = Image.getInstance(stream.toByteArray())
                for (pos in 0 until positions.size) {
                    val (x, y) = positions[pos]
                    image.setAbsolutePosition(x, y)
                    val content = stamper.getOverContent(index)
                    content.addImage(image)
                }
            }
        }

        stamper.close()
        reader.close()
        outputStream.close()
    }

    private fun getSignature(orderNumber: String = "123") {
        RetrofitClient.hyperoneApiService().getSignature(orderNumber)
            .enqueue(object : Callback<SignatureResponse> {
                override fun onResponse(
                    call: Call<SignatureResponse>,
                    response: Response<SignatureResponse>
                ) {
                    if (response.isSuccessful) {
                        val signatureList = response.body()?.signature

                        if (signatureList != null) {
                            // Handle the response
                            val receivingSignature = signatureList[0].receivingSignature
                            val storekeeperSignature = signatureList[0].storekeeperSignature


                            Log.i("TAG", "onResponse: $receivingSignature")

                            val byteArray = Base64.decode(receivingSignature, Base64.DEFAULT)
                            Log.i("TAG", "byteArray: ${byteArray.size.toString()}")

                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                            Log.i("TAG", "bitmap: ${bitmap.toString()}")
                            val imageView = findViewById<ImageView>(R.id.imageView2)
                            imageView.setImageBitmap(bitmap)
                        }
                    } else {
                        // Handle the error
                        Log.d("TAG", "onResponse() returned: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<SignatureResponse>, t: Throwable) {
                    // Handle the error
                    Log.d("TAG", "Handle the error: ${t.message}")
                }
            })
    }

    fun String.hexStringToByteArray(): ByteArray {
        val len = length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            val byte =
                ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
            data[i / 2] = byte
        }
        return data
    }
}