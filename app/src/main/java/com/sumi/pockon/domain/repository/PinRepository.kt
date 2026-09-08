package com.sumi.pockon.domain.repository

interface PinRepository {
    fun getPin(): String
    fun isEnabled(): Boolean
    fun save(pin: String)
    fun disable()
}
