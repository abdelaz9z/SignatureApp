package com.casecode.signature

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.github.gcacace.signaturepad.views.SignaturePad

class SignatureDialogFragment : DialogFragment() {

    private lateinit var signaturePad: SignaturePad
    private lateinit var btnClear: Button
    private lateinit var btnConfirmation: Button
    private lateinit var btnClose: Button
    private lateinit var tilte: TextView

    interface SignatureDialogListener {
        fun onSignatureClear()
        fun onSignatureSave(signatureBitmap: Bitmap)
        fun onClose()
    }

    private var listener: SignatureDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SignatureDialogListener) {
            listener = context
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_signature, null)

        signaturePad = view.findViewById(R.id.signaturePad)
        btnClear = view.findViewById(R.id.btnClear)
        btnConfirmation = view.findViewById(R.id.btnConfirmation)
        btnClose = view.findViewById(R.id.btnClose)
        tilte = view.findViewById(R.id.textViewTitle)



        // Get the string argument passed to the fragment and set it in the TextView
        val title = arguments?.getString("title")
        tilte.text = title

        btnClear.setOnClickListener {
            signaturePad.clear()
            listener?.onSignatureClear()
        }

        btnConfirmation.setOnClickListener {
            val signatureBitmap = signaturePad.signatureBitmap
            listener?.onSignatureSave(signatureBitmap)
            dismiss()
        }
        btnClose.setOnClickListener {
            listener?.onClose()
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
