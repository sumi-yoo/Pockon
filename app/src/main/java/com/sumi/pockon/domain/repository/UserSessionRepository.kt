package com.sumi.pockon.domain.repository

interface UserSessionRepository {
    fun uid(): String
    fun email(): String
    fun isGuest(): Boolean
    fun save(uid: String, email: String = "", name: String? = null, profileImage: String? = null, isGuest: Boolean = false)
    fun clear()
}
