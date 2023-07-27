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


class MainActivity : AppCompatActivity() , SignatureDialogFragment.SignatureDialogListener {

    private val PERMISSION_REQUEST_CODE = 100
    val signatureMap = mutableMapOf<String, Bitmap?>()
    lateinit var imageView :ImageView
    lateinit var imageView2 :ImageView

    lateinit var createPdfButton1 :Button
    lateinit var createPdfButton2 :Button
    lateinit var mSignaturePad:SignaturePad
    lateinit var KeeperSignedButton : Button
    lateinit var receiverSignedButton : Button
    lateinit var clearButton : Button
    lateinit var positions :List<Pair<Float,Float>>


    val orderID = "1111"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById<ImageView>(R.id.imageView3)
        imageView2 = findViewById<ImageView>(R.id.imageView2)
        createPdfButton1 = findViewById<Button>(R.id.create_pdf_1)
        createPdfButton2 = findViewById<Button>(R.id.create_pdf_2)
        mSignaturePad = findViewById<SignaturePad>(R.id.signature_pad)
        KeeperSignedButton = findViewById<Button>(R.id.btn_start_signing)
        receiverSignedButton = findViewById<Button>(R.id.btn_signed)
        clearButton = findViewById<Button>(R.id.btn_clear)
        positions = listOf(
            Pair(5f, 5f),   // Position for "KEEPER_KEY"
            //Pair(250f, 5f),  // Position for "WORKER_KEY"
            Pair(450f, 5f)   // Position for "STORE_KEY"
        )
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

        receiverSignedButton.setOnClickListener {
            val signatureBitmap = mSignaturePad.signatureBitmap
            val transparentSignatureBitmap = mSignaturePad.transparentSignatureBitmap
            val signatureSvg = mSignaturePad.signatureSvg

            if (!transparentSignatureBitmap.equals(null)) {
                saveReceiverSignatureOnServer(orderID,transparentSignatureBitmap)
                mSignaturePad.clear()
            }
        }
        KeeperSignedButton.setOnClickListener {
            val transparentSignatureBitmap = mSignaturePad.transparentSignatureBitmap
            if (!transparentSignatureBitmap.equals(null)) {
                saveStorekeeperSignatureToServer(orderID,transparentSignatureBitmap)
                mSignaturePad.clear()
            }
        }
        createPdfButton2.setOnClickListener {
            getSignatureFromServer("1111")
        }



        createPdfButton1.setOnClickListener {
            // Show the dialog when needed, passing the text you want to display in the dialog
            val dialogText = "Dialog Title"
            val dialog = SignatureDialogFragment()
            val bundle = Bundle()
            bundle.putString("title", dialogText)
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, "SignatureDialog")
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
                addImageInPdf(signatureMap , positions)
            } else {
                addImageInPdf(signatureMap , positions)
                // Permission denied, handle it accordingly (e.g., show an error message)
            }
        }
    }

    private fun saveReceiverSignatureOnServer(orderNumber: String, bitmap: Bitmap) {
        RetrofitClient.hyperoneApiService().receivingSignature(orderNumber, bitmapToString(bitmap))
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()

                        // Handle the response

                        Log.i("TAG", "receiver signature saved ")
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

    private fun saveStorekeeperSignatureToServer(orderNumber: String, bitmap: Bitmap) {
        RetrofitClient.hyperoneApiService().storekeeperSignature(orderNumber, bitmapToString(bitmap))
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()

                        // Handle the response

                        Log.i("TAG", "store keeper signature saved ")
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

    private fun checkWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                addImageInPdf(signatureMap , positions)
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
                addImageInPdf(signatureMap , positions)
            }
        } else {
            // For devices running below Android M, no runtime permission is required
            addImageInPdf(signatureMap , positions)
        }
    }

    private fun addImageInPdf(bitmaps: MutableMap<String, Bitmap?>, positions: List<Pair<Float, Float>>) {
        // Add image to PDF file
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(downloadsDir, "HyperOne.pdf")
        val outputStream = FileOutputStream(File(downloadsDir, "modified.pdf")) // Specify the output file name or path
        val reader = PdfReader(outputFile.inputStream())
        val stamper = PdfStamper(reader, outputStream)

        for (index in 1..reader.numberOfPages) {
            for ((key, bitmap) in bitmaps) {
                val stream = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val image = Image.getInstance(stream.toByteArray())
                if (key.equals( Constants.WORKER_KEY)){
                    val (x, y) = positions[0]
                    image.setAbsolutePosition(x, y)
                    val content = stamper.getOverContent(index)
                    content.addImage(image)
                }else{
                    val (x, y) = positions[1]
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

    private fun getSignatureFromServer(orderNumber: String) {
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
                            imageView.setImageBitmap(stringToBitmap(receivingSignature))
                            imageView2.setImageBitmap(stringToBitmap(storekeeperSignature))
                            val resizedReceivingSignature = Bitmap.createScaledBitmap(stringToBitmap(receivingSignature), 80, 80, false)
                            val resizedStorekeeperSignature = Bitmap.createScaledBitmap(stringToBitmap(storekeeperSignature), 80, 80, false)

                            signatureMap[Constants.KEEPER_KEY] = resizedReceivingSignature
                            signatureMap[Constants.WORKER_KEY] = resizedStorekeeperSignature
                            checkWriteStoragePermission()

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

    private fun bitmapToString(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // You can choose a different compression format and quality if desired
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun stringToBitmap(imageString: String): Bitmap {
        val imageData: ByteArray = Base64.decode(imageString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
    }


    //===================THE SIGNATURE DIALOG==============================

    override fun onSignatureClear() {
    }

    override fun onSignatureSave(signatureBitmap: Bitmap) {
    }

    override fun onClose() {
    }

}