package com.tri10.catboot.ui.main

import com.tri10.catboot.definition.Logger
import com.tri10.catboot.implementation.command.LoggerCommand

interface Composition {
    val logger: Logger
    val command: LoggerCommand
}