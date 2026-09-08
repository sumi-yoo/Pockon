package com.sumi.pockon.domain.repository

import android.content.Intent
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

interface AuthRepository {
    suspend fun getSignInIntent(accountName: String?): Result<Intent>
    suspend fun getGoogleCredential(): Result<GoogleIdTokenCredential>
    suspend fun signIn(idToken: String): Result<String>
    suspend fun deleteAccount(idToken: String?, credential: GoogleIdTokenCredential? = null): Result<Unit>
    fun signOut()
}
