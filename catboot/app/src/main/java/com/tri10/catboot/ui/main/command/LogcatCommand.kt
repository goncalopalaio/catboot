package com.tri10.catboot.ui.main.command

import androidx.annotation.WorkerThread
import com.tri10.catboot.definition.Logger
import com.tri10.catboot.definition.LoggerCommand
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

private val COMMAND = arrayOf("logcat", "-v", "time")
class LogcatCommand(private val logger: Logger, private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): LoggerCommand {
    private val processIsAlive = AtomicBoolean(false)
    private val _lines = MutableSharedFlow<String>()
    private val _errors = MutableSharedFlow<String>()

    override val lines: SharedFlow<String> = _lines
    override val errors: SharedFlow<String> = _errors

    // TODO Can we request READ_LOGS at runtime?.
    // TODO check READ_LOGS permission.

    @WorkerThread // Contains blocking operations.
    override suspend fun start(): Result<Int> {
        logger.debug("Building process")
        val processBuilder = ProcessBuilder(*COMMAND)
        val process = try {
            @Suppress("BlockingMethodInNonBlockingContext") // No non-blocking alternative.
            val runningProcess = processBuilder.start()
            processIsAlive.set(true)
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

        val jobs = listOf(
            scope.async {stdOutput(stdOutput)},
            scope.async {stdError(stdError)},
        )

        val exitCode = try {
            @Suppress("BlockingMethodInNonBlockingContext")
            process.waitFor()

        } catch (e: InterruptedException) {
            logger.debug("Interrupted | e=$e")

            processIsAlive.set(false)
            return Result.failure(e)
        }

        processIsAlive.set(false)
        jobs.awaitAll()

        return Result.success(exitCode)
    }

    private suspend fun stdError(stream: InputStream) {
        logger.debug("stdError | stream=$stream")
        readLines(stream) {
            // logger.debug("stdError: Read | it=$it")
            _errors.emit(it)
        }
        logger.debug("stdError exit")
    }

    private suspend fun stdOutput(stream: InputStream) {
        logger.debug("stdOutput | stream=$stream")
        readLines(stream) {
            // logger.debug("stdOutput: Read | it=$it")
            _lines.emit(it)
        }
        logger.debug("stdOutput exit")
    }

    private suspend fun readLines(stream: InputStream, callback: suspend (line: String) -> Any) {
        val reader = BufferedReader(InputStreamReader(stream))

        while(processIsAlive.get()) {
            // yield()

            val line = try {
                @Suppress("BlockingMethodInNonBlockingContext") // IMPROVEMENT: Maybe we can find a non-blocking alternative?
                reader.readLine()

            } catch (e: IOException) {
                logger.debug("Failed to read line | e=$e")
                continue
            }

            callback(line)
        }
    }
}