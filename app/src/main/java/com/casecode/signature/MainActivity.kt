package com.casecode.signature

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.gcacace.signaturepad.views.SignaturePad
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mSignaturePad = findViewById<SignaturePad>(R.id.signature_pad)
        val startSigningButton = findViewById<Button>(R.id.btn_start_signing)
        val signedButton = findViewById<Button>(R.id.btn_signed)
        val clearButton = findViewById<Button>(R.id.btn_clear)

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

            addImageInPdfNew(transparentSignatureBitmap)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    private fun addImageInPdfNew(bitmap: Bitmap) {
        // Add image to PDF file
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(downloadsDir, "test2.pdf")

        val outputStream = FileOutputStream(File(downloadsDir, "modified.pdf")) // Specify the output file name or path

        val reader = PdfReader(outputFile.inputStream())
        val stamper = PdfStamper(reader, outputStream)

        val page = 1 // Specify the page number where you want to add the image

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = Image.getInstance(stream.toByteArray())
        image.setAbsolutePosition(100f, 100f) // Specify the position of the image on the page

        val content = stamper.getOverContent(page)
        content.addImage(image)

        stamper.close()
        reader.close()
        outputStream.close()
    }


}