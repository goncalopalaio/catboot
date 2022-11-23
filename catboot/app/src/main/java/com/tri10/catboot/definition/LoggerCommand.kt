package com.tri10.catboot.definition

import kotlinx.coroutines.flow.SharedFlow


interface LoggerCommand {
    val lines: SharedFlow<String>
    val errors: SharedFlow<String>

    suspend fun start(): Result<Int>
}