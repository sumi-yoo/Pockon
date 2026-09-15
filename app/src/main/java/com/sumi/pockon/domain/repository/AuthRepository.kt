package com.sumi.pockon.domain.repository

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

interface AuthRepository {
    suspend fun getGoogleCredential(): Result<GoogleIdTokenCredential>
    suspend fun signIn(idToken: String): Result<String>
    suspend fun deleteAccount(credential: GoogleIdTokenCredential): Result<Unit>
    fun signOut()
}
