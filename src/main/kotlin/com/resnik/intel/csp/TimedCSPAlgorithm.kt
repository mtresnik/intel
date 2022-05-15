package com.resnik.intel.csp

abstract class TimedCSPAlgorithm {

    private var start: Long = System.currentTimeMillis()
    private var dt: Long = 0L

    fun onStart() {
        start = System.currentTimeMillis()
    }

    fun onFinish() {
        dt = timeElapsed()
    }

    protected fun timeElapsed() = System.currentTimeMillis() - start

    open fun finalTime() = dt

    @Throws(CSPException::class)
    fun onException(ex: CSPException) {
        dt = -1; throw ex
    }

}