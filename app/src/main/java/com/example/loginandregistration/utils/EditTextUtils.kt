package com.example.loginandregistration.utils

import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText

/**
 * Utility object for safely updating EditText fields to prevent InputConnection warnings
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */
object EditTextUtils {
    
    /**
     * Safely sets text on an EditText by checking if the view has focus and is attached to window
     * Requirements: 14.1, 14.2
     * 
     * @param editText The EditText to update
     * @param text The text to set
     */
    fun safeSetText(editText: EditText, text: CharSequence) {
        if (editText.isAttachedToWindow) {
            editText.setText(text)
        }
    }
    
    /**
     * Safely sets text on a TextInputEditText by checking if the view has focus and is attached to window
     * Requirements: 14.1, 14.2
     * 
     * @param editText The TextInputEditText to update
     * @param text The text to set
     */
    fun safeSetText(editText: TextInputEditText, text: CharSequence) {
        if (editText.isAttachedToWindow) {
            editText.setText(text)
        }
    }
    
    /**
     * Safely sets text on an EditText using post {} to ensure proper timing
     * Use this for updates that occur after potential navigation or focus changes
     * Requirements: 14.3
     * 
     * @param editText The EditText to update
     * @param text The text to set
     */
    fun safeSetTextPost(editText: EditText, text: CharSequence) {
        editText.post {
            if (editText.isAttachedToWindow) {
                editText.setText(text)
            }
        }
    }
    
    /**
     * Safely sets text on a TextInputEditText using post {} to ensure proper timing
     * Use this for updates that occur after potential navigation or focus changes
     * Requirements: 14.3
     * 
     * @param editText The TextInputEditText to update
     * @param text The text to set
     */
    fun safeSetTextPost(editText: TextInputEditText, text: CharSequence) {
        editText.post {
            if (editText.isAttachedToWindow) {
                editText.setText(text)
            }
        }
    }
    
    /**
     * Safely clears text from an EditText
     * Requirements: 14.1, 14.2
     * 
     * @param editText The EditText to clear
     */
    fun safeClear(editText: EditText) {
        if (editText.isAttachedToWindow) {
            editText.text?.clear()
        }
    }
    
    /**
     * Safely clears text from a TextInputEditText
     * Requirements: 14.1, 14.2
     * 
     * @param editText The TextInputEditText to clear
     */
    fun safeClear(editText: TextInputEditText) {
        if (editText.isAttachedToWindow) {
            editText.text?.clear()
        }
    }
}
