package com.tri10.catboot

import com.tri10.catboot.definition.LogSource
import com.tri10.catboot.definition.Logger
import com.tri10.catboot.definition.StreamSource
import java.io.PipedInputStream
import java.io.PipedOutputStream


class WorkingSource(private val logger: Logger): LogSource {
    // Quick and dirty solution to pass continuously pass data through to an inputstream - https://blog.ostermiller.org/convert-a-java-outputstream-to-an-inputstream/
    private val writeOutputStream = PipedOutputStream()
    private val writeErrorOutputStream = PipedOutputStream()

    private val stdInput = PipedInputStream(writeOutputStream)
    private val stdError = PipedInputStream(writeErrorOutputStream)

    private val lockingObject = Object()
    @Volatile
    private var isRunning = true

    override fun start(): Result<StreamSource> {
        isRunning = true
        logger.debug("start | isRunning=$isRunning")
        return Result.success(StreamSource(stdInput, stdError))
    }

    override fun waitFor(): Result<Int> {
        logger.debug("waitFor | isRunning=$isRunning")
        synchronized(lockingObject) {
            while(isRunning) {
                logger.debug("waitFor synchronized | isRunning=$isRunning")
                lockingObject.wait()
            }
        }

        return Result.success(0)
    }

    override fun stop(): Result<Int> {
        synchronized(lockingObject) {
            isRunning = false
            lockingObject.notify()
        }
        return Result.success(0)
    }

    fun createNewLine(text: String) {
        logger.debug("createNewLine | text=$text")
        writeOutputStream.write(text.toByteArray())
        writeOutputStream.flush()
    }

    fun createNewError(text: String) {
        logger.debug("createNewError | text=$text")
        writeErrorOutputStream.write(text.toByteArray())
        writeOutputStream.flush()
    }
}