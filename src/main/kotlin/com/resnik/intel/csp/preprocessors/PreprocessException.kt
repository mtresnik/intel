package com.resnik.intel.csp.preprocessors

import com.resnik.intel.csp.CSPException

class PreprocessException(message: String?, cause: Throwable?) : CSPException(message, cause) {

    constructor(message: String?) : this(message, null)

    constructor(cause: Throwable?) : this(cause?.toString(), cause)

    constructor() : this(null, null)

}