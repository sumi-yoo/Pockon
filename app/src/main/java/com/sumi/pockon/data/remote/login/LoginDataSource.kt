package com.sumi.pockon.data.remote.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {

    suspend fun login(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return requireNotNull(auth.signInWithCredential(credential).await().user?.uid) { "User ID is required." }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun removeAccount(idToken: String) {
        val user = requireNotNull(auth.currentUser) { "Signed-in user is required." }
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        user.reauthenticate(firebaseCredential).await()
        user.delete().await()
    }
}
