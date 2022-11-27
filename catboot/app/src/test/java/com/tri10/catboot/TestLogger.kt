package com.tri10.catboot

import com.tri10.catboot.definition.Logger

class TestLogger(val isEnabled: Boolean): Logger {
    override fun debug(text: String) {
        if (!isEnabled) return
        println(text)
    }
}