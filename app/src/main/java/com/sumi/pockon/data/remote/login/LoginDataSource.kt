package com.sumi.pockon.data.remote.login

import android.content.Context
import android.content.Intent
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sumi.pockon.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    private val credentialManager = CredentialManager.create(context)

    private val googleIdOption = GetGoogleIdOption
        .Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
        .setAutoSelectEnabled(false)
        .build()

    private val request = GetCredentialRequest
        .Builder()
        .addCredentialOption(googleIdOption)
        .build()

    suspend fun getSignInIntent(accountName: String?): Intent {
        // 반드시 signOut을 먼저 호출해줘야 다중 계정 선택 가능
        val gso = if (accountName.isNullOrEmpty()) {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build()
        } else {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                .requestEmail()
                .setAccountName(accountName) // 현재 로그인된 계정 고정
                .build()
        }

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut().await()
        return googleSignInClient.signInIntent
    }

    suspend fun getIdToken(): GoogleIdTokenCredential {
        try {
            val credential = credentialManager.getCredential(
                request = request,
                context = context
            ).credential
            val googleCredential = when (credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            GoogleIdTokenCredential.createFrom(credential.data)
                        } catch (e: GoogleIdTokenParsingException) {
                            null
                        }
                    } else {
                        null
                    }
                }
                else -> {
                    null
                }
            }
            credentialManager.clearCredentialState(request = ClearCredentialStateRequest())
            return requireNotNull(googleCredential) { "Google credential is required." }
        } catch (e: GetCredentialException) {
            throw e
        }
    }

    suspend fun login(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return requireNotNull(auth.signInWithCredential(credential).await().user?.uid) { "User ID is required." }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun removeAccount(idToken: String?, selectedCredential: GoogleIdTokenCredential? = null) {
        require(!idToken.isNullOrEmpty()) { "ID token is required." }
        val user = requireNotNull(auth.currentUser) { "Signed-in user is required." }
        require(selectedCredential == null || selectedCredential.id == idToken) { "Selected account does not match." }

        val credential = if (selectedCredential != null) {
            GoogleAuthProvider.getCredential(selectedCredential.idToken, null)
        } else {
            GoogleAuthProvider.getCredential(idToken, null)
        }
        user.reauthenticate(credential).await()
        user.delete().await()
    }
}
