package com.sumi.pockon.core.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sumi.pockon.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCredentialProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GoogleCredentialProvider {
    private val credentialManager = CredentialManager.create(context)
    private val request = GetCredentialRequest.Builder()
        .addCredentialOption(
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()
        )
        .build()

    override suspend fun getCredential(): Result<GoogleAccount> = try {
        val credential = credentialManager.getCredential(context, request).credential
        val googleCredential = (credential as? CustomCredential)
            ?.takeIf { it.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL }
            ?.let { GoogleIdTokenCredential.createFrom(it.data) }
            ?: error("Google credential is required.")

        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        Result.success(
            GoogleAccount(
                idToken = googleCredential.idToken,
                email = googleCredential.id,
                displayName = googleCredential.displayName,
                profileImageUrl = googleCredential.profilePictureUri?.toString()
            )
        )
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
