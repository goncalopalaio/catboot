package com.tri10.catboot.definition

import java.io.InputStream

data class StreamSource(val inputStream: InputStream, val errorStream: InputStream)

interface LogSource {

    fun start(): Result<StreamSource>

    fun waitFor(): Result<Int>

    fun stop(): Result<Int>
}