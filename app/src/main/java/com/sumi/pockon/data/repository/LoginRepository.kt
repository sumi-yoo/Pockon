package com.sumi.pockon.data.repository

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sumi.pockon.data.remote.login.LoginDataSource
import com.sumi.pockon.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val loginDataSource: LoginDataSource
) : AuthRepository {

    override suspend fun getSignInIntent(accountName: String?) = resultOf {
        loginDataSource.getSignInIntent(accountName)
    }

    override suspend fun getGoogleCredential() = resultOf { loginDataSource.getIdToken() }

    override suspend fun signIn(idToken: String) = resultOf { loginDataSource.login(idToken) }

    override suspend fun deleteAccount(idToken: String?, credential: GoogleIdTokenCredential?) = resultOf {
        loginDataSource.removeAccount(idToken, credential)
    }

    override fun signOut() {
        loginDataSource.logout()
    }

    private suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
