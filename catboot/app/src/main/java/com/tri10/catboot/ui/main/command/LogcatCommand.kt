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
import kotlin.system.measureTimeMillis

private val COMMAND = arrayOf("logcat", "-v", "time")
class LogcatCommand(private val logger: Logger, private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): LoggerCommand {
    private val processIsAlive = AtomicBoolean(false)
    private val _isReading = MutableSharedFlow<String>()
    private val _lines = MutableSharedFlow<String>()
    private val _errors = MutableSharedFlow<String>()

    override val lines: SharedFlow<String> = _lines
    override val errors: SharedFlow<String> = _errors
    override val isReading: SharedFlow<String> = _isReading

    // TODO Can we request READ_LOGS at runtime?.
    // TODO check READ_LOGS permission.

    @WorkerThread // Contains blocking operations.
    override suspend fun start(): Result<Int> {
        logger.debug("Building process")
        val processBuilder = ProcessBuilder(*COMMAND)
        val process = try {
            @Suppress("BlockingMethodInNonBlockingContext") // No non-blocking alternative.
            val runningProcess = processBuilder.start()
            setProcessState(true)
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
            scope.launch {stdOutput(stdOutput)},
            scope.launch {stdError(stdError)},
        )

        val exitCode = try {
            logger.debug("waitFor")
            @Suppress("BlockingMethodInNonBlockingContext")
            process.waitFor()

        } catch (e: InterruptedException) {
            logger.debug("Interrupted | e=$e")

            setProcessState(false)
            return Result.failure(e)
        }

        setProcessState(false)
        logger.debug("joinAll")
        jobs.joinAll()

        logger.debug("exitCode=$exitCode")
        return Result.success(exitCode)
    }

    private suspend fun stdError(stream: InputStream) {
        logger.debug("stdError start | stream=$stream")
        readLines(stream) {
            // logger.debug("stdError: Read | it=$it")
            _errors.emit(it)
        }
        logger.debug("stdError exit")
    }

    private suspend fun stdOutput(stream: InputStream) {
        logger.debug("stdOutput start | stream=$stream")
        readLines(stream) {
            // logger.debug("stdOutput: Read | it=$it")
            _lines.emit(it)
        }
        logger.debug("stdOutput exit")
    }

    @Suppress("BlockingMethodInNonBlockingContext") // IMPROVEMENT: Maybe we can find a non-blocking alternative for the reader?
    private suspend fun readLines(stream: InputStream, callback: suspend (line: String) -> Any) {
        val reader = BufferedReader(InputStreamReader(stream))

        while(processIsAlive.get()) {
            val startTimeMs = System.currentTimeMillis()
            // yield()

            val line = try {
                reader.readLine()
            } catch (e: IOException) {
                logger.debug("Failed to read line | e=$e")
                continue
            }

            if (line == null) {
                logger.debug("No line")
                delay(1000)
                continue
            }

            callback(line)
            val diffTimeMs = System.currentTimeMillis() - startTimeMs
            _isReading.emit("${diffTimeMs}ms")
        }

        @Suppress("BlockingMethodInNonBlockingContext")
        reader.close()
    }

    private suspend fun setProcessState(isAlive: Boolean) {
        logger.debug("setProcessState | isAlive=$isAlive")
        processIsAlive.set(isAlive)
        _isReading.emit(if (isAlive) "Reading" else "Not Reading")
    }
}