package com.tri10.catboot.ui.main.logging

import android.util.Log
import com.tri10.catboot.definition.Logger

private const val TAG = "Catboot"
class AndroidLogger: Logger {
    override fun debug(text: String) {
        Log.d(TAG, text)
    }
}