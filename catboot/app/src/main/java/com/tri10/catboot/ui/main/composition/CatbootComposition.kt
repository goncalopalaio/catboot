package com.tri10.catboot.ui.main.composition

import android.util.Log
import com.tri10.catboot.definition.Logger
import com.tri10.catboot.definition.LoggerCommand
import com.tri10.catboot.ui.main.Composition
import com.tri10.catboot.ui.main.command.LogcatCommand
import com.tri10.catboot.ui.main.logging.AndroidLogger

object CatbootComposition: Composition {

    override val logger: Logger = AndroidLogger()
    override val command: LoggerCommand = LogcatCommand(logger)
}