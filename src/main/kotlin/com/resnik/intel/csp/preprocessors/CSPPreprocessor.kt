package com.resnik.intel.csp.preprocessors

import com.resnik.intel.csp.CSPBase
import com.resnik.intel.csp.CSPException
import com.resnik.intel.csp.TimedCSPAlgorithm

abstract class CSPPreprocessor<VAR, DOMAIN> : TimedCSPAlgorithm() {

    @Throws(CSPException.DomainException::class)
    abstract fun preprocess(csp: CSPBase<VAR, DOMAIN>)

}