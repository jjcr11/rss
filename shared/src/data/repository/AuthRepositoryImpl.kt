package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.preferences.UserPreferencesDataSource
import com.jjcr11.rss.data.remote.datasource.AuthRemoteDataSource
import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.model.User
import com.jjcr11.rss.domain.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val sourceDao: SourceDao? = null,
    private val userPreferencesDataSource: UserPreferencesDataSource? = null,
) : AuthRepository {

    override val authState: Flow<AuthState> =
        authRemoteDataSource.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    if (user != null) {
                        AuthState.Authenticated(
                            user = User(
                                id = Uuid.parse(uuidString = user.id),
                                email = user.email,
                            ),
                        )
                    } else {
                        AuthState.Unauthenticated
                    }
                }
                is SessionStatus.NotAuthenticated,
                is SessionStatus.RefreshFailure,
                -> AuthState.Unauthenticated

                SessionStatus.Initializing -> AuthState.Initializing
            }
        }

    override suspend fun signUpWithEmail(email: String, password: String) {
        authRemoteDataSource.signUp(email = email, password = password)
    }

    override suspend fun signInWithEmail(email: String, password: String) {
        authRemoteDataSource.signIn(email = email, password = password)
    }

    override suspend fun authenticateWithEmail(email: String, password: String) {
        try {
            authRemoteDataSource.signIn(email = email, password = password)
        } catch (signInException: Exception) {
            if (isInvalidCredentialsError(e = signInException)) {
                try {
                    authRemoteDataSource.signUp(email = email, password = password)
                } catch (signUpException: Exception) {
                    if (isUserAlreadyExistsError(e = signUpException)) {
                        throw signInException
                    } else {
                        throw signUpException
                    }
                }
            } else {
                throw signInException
            }
        }
    }

    private fun isInvalidCredentialsError(e: Exception): Boolean {
        if (e !is RestException) {
            return false
        }
        val isInvalidGrant = e.error.contains(other = "invalid_grant", ignoreCase = true)
        val isInvalidCredentialsDesc = e.description?.contains(
            other = "Invalid login credentials",
            ignoreCase = true,
        ) == true
        val isInvalidCredentialsMsg = e.message?.contains(
            other = "Invalid login credentials",
            ignoreCase = true,
        ) == true
        return (e.statusCode == 400) || isInvalidGrant || isInvalidCredentialsDesc || isInvalidCredentialsMsg
    }

    private fun isUserAlreadyExistsError(e: Exception): Boolean {
        if (e !is RestException) {
            return false
        }
        val isUserAlreadyExists = e.error.contains(other = "user_already_exists", ignoreCase = true)
        val isAlreadyDesc = e.description?.contains(other = "already registered", ignoreCase = true) == true
        val isAlreadyMsg = e.message?.contains(other = "already registered", ignoreCase = true) == true
        return isUserAlreadyExists || isAlreadyDesc || isAlreadyMsg
    }

    override suspend fun logout() {
        try {
            authRemoteDataSource.signOut()
        } catch (_: Exception) {
            // Silently catch network or session errors on remote signout
        }
        sourceDao?.clearAll()
        userPreferencesDataSource?.clear()
    }
}
