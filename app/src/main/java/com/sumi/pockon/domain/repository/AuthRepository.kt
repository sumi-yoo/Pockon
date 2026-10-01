package com.sumi.pockon.domain.repository

interface AuthRepository {
    suspend fun signIn(idToken: String): Result<String>
    suspend fun deleteAccount(idToken: String): Result<Unit>
    fun signOut()
}
