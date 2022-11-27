package com.tri10.catboot.implementation.command

import com.tri10.catboot.definition.LogSource
import com.tri10.catboot.definition.Logger
import com.tri10.catboot.definition.StreamSource
import java.io.IOException

private val COMMAND = arrayOf("logcat", "-v", "time")

class LogcatSource(private val logger: Logger) : LogSource {
    private var process: Process? = null

    override fun start(): Result<StreamSource> {
        logger.debug("Building process")
        val processBuilder = ProcessBuilder(*COMMAND)
        val process = try {
            @Suppress("BlockingMethodInNonBlockingContext") // No non-blocking alternative.
            val runningProcess = processBuilder.start()
            runningProcess

        } catch (e: IOException) {
            logger.debug("Failed to build process | e=$e")
            return Result.failure(e)
        }

        val stdOutput = process.inputStream
        val stdError = process.errorStream

        logger.debug("stdOutput=$stdOutput, stdError=$stdError")

        if (stdOutput == null) return Result.failure(Exception("No stream to STDOUT")) // TODO - We don't really need the stacktrace from the exception.
        if (stdError == null) return Result.failure(Exception("No stream to STDERR")) // TODO - We don't really need the stacktrace from the exception.

        this.process = process
        return Result.success(StreamSource(stdOutput, stdError))
    }

    override fun waitFor(): Result<Int> {
        val currentProcess =
            process ?: return Result.failure(Exception("waitFor | No running process"))

        return try {
            Result.success(currentProcess.waitFor())
        } catch (e: InterruptedException) {
            Result.failure(e)
        }
    }

    override fun stop(): Result<Int> {
        process?.let {
            it.destroy()
            val exitCode = it.exitValue()
            return Result.success(exitCode)
        }
        return Result.failure(Exception("No running process"))
    }
}