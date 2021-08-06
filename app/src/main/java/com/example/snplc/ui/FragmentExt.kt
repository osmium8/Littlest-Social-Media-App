package com.example.snplc.ui

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

/**
 * saves code
 * can be called from fragments to call snackbar
 */
fun Fragment.snackbar(text: String) {
    Snackbar.make(
        requireView(),
        text,
        Snackbar.LENGTH_LONG
    ).show()
}