package com.resnik.intel.csp

open class CSPException(message: String?, cause: Throwable?) : Exception(message, cause) {

    constructor(message: String?) : this(message, null)

    constructor(cause: Throwable?) : this(cause?.toString(), cause)

    constructor() : this(null, null)

    open class DomainException(message: String?) : CSPException(message)

    open class InvalidDomainException(message: String?) : DomainException(message)

    open class EmptyDomainException(message: String?) : InvalidDomainException(message)

}