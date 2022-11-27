package com.tri10.catboot

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.CharBuffer

/**
 * Tests basic behaviour when reading from a [com.tri10.catboot.definition.LogSource].
 *
 * Goal: Handle extraneous logcat behaviors when the process dies or errors out in the middle of writing logs.
 */
@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalCoroutinesApi::class)
class LoggerSourceUnitTest {
    private val logger = TestLogger(true)

    @Test
    fun `lines are propagated - sanity check`() = runTest {
        val source = WorkingSource(logger)

        val runningSourceResult = source.start()

        assertTrue(runningSourceResult.isSuccess)
        val runningSource = runningSourceResult.getOrThrow()

        launch {
            withTimeout(2_000) {
                logger.debug("reading")
                val reader = BufferedReader(InputStreamReader(runningSource.inputStream))
                val line = reader.readLine()
                assertEquals("A", line.toString()) // TODO will we get into problems by waiting for a \n in each logcat line?

                val stopResult = source.stop()
                assertTrue(stopResult.isSuccess)
                logger.debug("stopped")
            }
        }

        source.createNewLine("A\n")
        source.createNewLine("B\n")
        source.createNewLine("C\n")
        logger.debug("waitFor")
        val waitingJob = launch { source.waitFor() }
        waitingJob.join()
        logger.debug("Exited")
    }
}