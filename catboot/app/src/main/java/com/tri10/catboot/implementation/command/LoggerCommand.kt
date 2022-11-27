package com.tri10.catboot.implementation.command

import androidx.annotation.WorkerThread
import com.tri10.catboot.definition.LogSource
import com.tri10.catboot.definition.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

private const val RETRY_INTERVAL_MS = 5000L

class LoggerCommand(
    private val logger: Logger,
    private val source: LogSource,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val processIsAlive = AtomicBoolean(false)
    private val _isReading = MutableSharedFlow<String>()
    private val _lines = MutableSharedFlow<String>()
    private val _errors = MutableSharedFlow<String>()

    val lines: SharedFlow<String> = _lines
    val errors: SharedFlow<String> = _errors
    val isReading: SharedFlow<String> = _isReading

    // TODO Can we request READ_LOGS at runtime?.
    // TODO check READ_LOGS permission.

    @WorkerThread // Contains blocking operations.
    suspend fun start(): Result<Int> {
        val jobs = mutableListOf<Job>()

        while (true) {
            yield()

            jobs.forEach { it.cancel(cause = CancellationException("Starting new source")) }
            jobs.joinAll()

            val runningSourceResult = source.start()
            val runningSource = if (runningSourceResult.isFailure) {
                logger.debug("error | ${runningSourceResult.exceptionOrNull()}")
                delay(RETRY_INTERVAL_MS)
                continue
            } else {
                val result = runningSourceResult.getOrNull()
                if (result == null) {
                    logger.debug("result is null") // Note: it would be safe to do getOrThrow but we're keeping this to avoid a loose exception. Maybe replace this with a development assert?
                    delay(RETRY_INTERVAL_MS)
                    continue
                } else {
                    result
                }
            }

            val (stdOutput, stdError) = runningSource
            jobs.clear()
            jobs.add(scope.launch { stdOutput(stdOutput) })
            jobs.add(scope.launch { stdOutput(stdError) })

            setProcessState(true)
            val exitCode = try {
                logger.debug("waitFor")
                @Suppress("BlockingMethodInNonBlockingContext")
                source.waitFor()

            } catch (e: InterruptedException) {
                logger.debug("Interrupted | e=$e")

                setProcessState(false)
                return Result.failure(e)
            }

            logger.debug("Exited | exitCode=$exitCode")
        }
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

        while (processIsAlive.get()) {
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