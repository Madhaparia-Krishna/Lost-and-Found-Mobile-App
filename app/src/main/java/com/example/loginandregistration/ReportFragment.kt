package com.example.loginandregistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class ReportFragment : Fragment() {

    private lateinit var etItemName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etLocation: EditText
    private lateinit var etContactInfo: EditText
    private lateinit var rgItemType: RadioGroup
    private lateinit var btnSubmit: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var isForSecurity: Boolean = false

    // --- ADD THIS COMPANION OBJECT ---
    companion object {
        private const val ARG_IS_SECURITY = "is_security_creating_report"

        /**
         * Use this factory method to create a new instance of
         * this fragment.
         *
         * @param isSecurityCreatingReport True if the security staff is creating a report.
         * @return A new instance of fragment ReportFragment.
         */
        @JvmStatic
        fun newInstance(isSecurityCreatingReport: Boolean = false): ReportFragment {
            val fragment = ReportFragment()
            val args = Bundle()
            args.putBoolean(ARG_IS_SECURITY, isSecurityCreatingReport)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the argument here
        arguments?.let {
            isForSecurity = it.getBoolean(ARG_IS_SECURITY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etItemName = view.findViewById(R.id.et_item_name)
        etDescription = view.findViewById(R.id.et_description)
        etLocation = view.findViewById(R.id.et_location)
        etContactInfo = view.findViewById(R.id.et_contact_info)
        rgItemType = view.findViewById(R.id.rg_item_type)
        btnSubmit = view.findViewById(R.id.btn_submit)

        // You can now use the 'isForSecurity' flag to change the UI or logic
        if (isForSecurity) {
            // Example: Change the submit button text for security
            btnSubmit.text = "Create Found Item Report"
        }

        btnSubmit.setOnClickListener {
            submitReport()
        }
    }

    private fun submitReport() {
        val itemName = etItemName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val contactInfo = etContactInfo.text.toString().trim()

        if (itemName.isEmpty() || description.isEmpty() || location.isEmpty()) {
            // Contact info might not be needed if security is filing the report
            if (!isForSecurity && contactInfo.isEmpty()) {
                Toast.makeText(context, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return
            } else if (isForSecurity && (itemName.isEmpty() || description.isEmpty() || location.isEmpty())) {
                Toast.makeText(context, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return
            }
        }

        val isLost = rgItemType.checkedRadioButtonId == R.id.rb_lost
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val item = LostFoundItem(
            name = itemName,
            description = description,
            location = location,
            contactInfo = if (isForSecurity) "Held by Security" else contactInfo, // Adjust contact info for security
            isLost = isLost,
            userId = currentUser.uid,
            userEmail = currentUser.email ?: "",
            timestamp = Timestamp.now()
        )

        db.collection("items")
            .add(item)
            .addOnSuccessListener {
                Toast.makeText(context, getString(R.string.item_reported_successfully), Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        etItemName.text.clear()
        etDescription.text.clear()
        etLocation.text.clear()
        etContactInfo.text.clear()
        rgItemType.clearCheck()
    }
}
