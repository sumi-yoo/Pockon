package com.sumi.pockon.data.repository

import com.sumi.pockon.data.remote.login.LoginDataSource
import com.sumi.pockon.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val loginDataSource: LoginDataSource
) : AuthRepository {

    override suspend fun signIn(idToken: String) = resultOf { loginDataSource.login(idToken) }

    override suspend fun deleteAccount(idToken: String) = resultOf {
        loginDataSource.removeAccount(idToken)
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
