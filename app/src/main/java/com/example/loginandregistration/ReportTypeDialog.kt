package com.example.loginandregistration

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Dialog for selecting report type (Lost or Found)
 * Requirements: 3.3
 */
class ReportTypeDialog : DialogFragment() {

    private var onTypeSelectedListener: ((String) -> Unit)? = null

    companion object {
        private const val TAG = "ReportTypeDialog"

        fun newInstance(onTypeSelected: (String) -> Unit): ReportTypeDialog {
            return ReportTypeDialog().apply {
                this.onTypeSelectedListener = onTypeSelected
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_report_type, null)

        val btnReportLost = view.findViewById<Button>(R.id.btn_report_lost)
        val btnReportFound = view.findViewById<Button>(R.id.btn_report_found)

        btnReportLost.setOnClickListener {
            onTypeSelectedListener?.invoke(ReportFragment.TYPE_LOST)
            dismiss()
        }

        btnReportFound.setOnClickListener {
            onTypeSelectedListener?.invoke(ReportFragment.TYPE_FOUND)
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Return null to use the dialog created in onCreateDialog
        return null
    }
}
