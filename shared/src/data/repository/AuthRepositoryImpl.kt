package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.remote.datasource.AuthRemoteDataSource
import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.model.User
import com.jjcr11.rss.domain.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
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
}
