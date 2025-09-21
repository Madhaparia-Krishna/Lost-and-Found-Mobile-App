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
        
        btnSubmit.setOnClickListener {
            submitReport()
        }
    }
    
    private fun submitReport() {
        val itemName = etItemName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val contactInfo = etContactInfo.text.toString().trim()
        
        if (itemName.isEmpty() || description.isEmpty() || location.isEmpty() || contactInfo.isEmpty()) {
            Toast.makeText(context, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
            return
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
            contactInfo = contactInfo,
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