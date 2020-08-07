package com.jetbrains.mobile.execution

import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.util.Key
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicInteger

class CompositeProcessHandler(val handlers: List<ProcessHandler>) : ProcessHandler() {
    private val terminatedCount = AtomicInteger()

    inner class EachListener : ProcessAdapter() {
        override fun startNotified(event: ProcessEvent) {
            if (!this@CompositeProcessHandler.isStartNotified) {
                this@CompositeProcessHandler.startNotify()
            }
        }

        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
            this@CompositeProcessHandler.notifyTextAvailable(event.text, outputType)
        }

        override fun processTerminated(event: ProcessEvent) {
            val terminated = terminatedCount.incrementAndGet()
            if (terminated == handlers.size) {
                this@CompositeProcessHandler.notifyProcessTerminated(0)
            }
        }
    }

    init {
        assert(handlers.isNotEmpty())
        handlers.forEach { it.addProcessListener(EachListener()) }
    }

    override fun destroyProcessImpl() {
        handlers.forEach { it.destroyProcess() }
    }

    override fun detachProcessImpl() {
        handlers.forEach { it.detachProcess() }
    }

    override fun detachIsDefault(): Boolean = false
    override fun getProcessInput(): OutputStream? = null
}
