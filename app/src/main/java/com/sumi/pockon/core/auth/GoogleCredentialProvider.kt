package com.sumi.pockon.core.auth

data class GoogleAccount(
    val idToken: String,
    val email: String,
    val displayName: String?,
    val profileImageUrl: String?
)

interface GoogleCredentialProvider {
    suspend fun getCredential(): Result<GoogleAccount>
}
