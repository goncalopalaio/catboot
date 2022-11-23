package com.tri10.catboot.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tri10.catboot.definition.Logger
import com.tri10.catboot.definition.LoggerCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val logger: Logger, private val loggerCommand: LoggerCommand) : ViewModel() {
    val newLogLine = loggerCommand.lines.asLiveData()
    val newErrorLine = loggerCommand.errors.asLiveData()

    fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            logger.debug("Starting")
            val result = loggerCommand.start()
            logger.debug("Finished | result=$result")
        }
    }
}